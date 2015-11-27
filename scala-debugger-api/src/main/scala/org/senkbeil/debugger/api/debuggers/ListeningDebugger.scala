package org.senkbeil.debugger.api.debuggers

import com.sun.jdi._
import com.sun.jdi.connect.{Connector, ListeningConnector}
import org.senkbeil.debugger.api.profiles.ProfileManager
import org.senkbeil.debugger.api.utils.{LoopingTaskRunner, Logging}
import org.senkbeil.debugger.api.virtualmachines.ScalaVirtualMachine

import scala.collection.JavaConverters._
import scala.util.Try

object ListeningDebugger {
  /**
   * Creates a new instance of the listening debugger. Defaults to using a
   * single thread executor with one worker.
   *
   * @param virtualMachineManager The manager to use for virtual machine
   *                              connectors
   * @param address The address to use for remote JVMs to attach to this
   *                debugger
   * @param port The port to use for remote JVMs to attach to this debugger
   * @param workers The total number of worker tasks to spawn
   */
  def apply(
    address: String,
    port: Int,
    workers: Int = 1
  )(
    implicit virtualMachineManager: VirtualMachineManager =
      Bootstrap.virtualMachineManager()
  ): ListeningDebugger = new ListeningDebugger(
    virtualMachineManager,
    () => new ProfileManager,
    new LoopingTaskRunner(initialWorkers = workers),
    address,
    port,
    workers
  )
}

/**
 * Represents a debugger that listens for connections from remote JVMs.
 *
 * @param virtualMachineManager The manager to use for virtual machine
 *                              connectors
 * @param newProfileManagerFunc The function to be executed per new VM
 *                              connection that generates a profile manager for
 *                              each new ScalaVirtualMachine
 * @param loopingTaskRunner The task runner to use with the ScalaVirtualMachines
 *                          created from the listening VMs
 * @param address The address to use for remote JVMs to attach to this debugger
 * @param port The port to use for remote JVMs to attach to this debugger
 * @param workers The total number of worker tasks to spawn
 */
class ListeningDebugger private[debugger] (
  private val virtualMachineManager: VirtualMachineManager,
  private val newProfileManagerFunc: () => ProfileManager,
  private val loopingTaskRunner: LoopingTaskRunner,
  private val address: String,
  private val port: Int,
  private val workers: Int
) extends Debugger with Logging {
  private val ConnectorClassString = "com.sun.jdi.SocketListen"

  // Contains all components for the currently-running debugger
  @volatile private var components: Option[(
    LoopingTaskRunner, ListeningConnector, Map[String, _ <: Connector.Argument]
  )] = None

  /**
   * Represents the JVM options to feed to remote JVMs whom will connect to
   * this debugger.
   */
  val remoteJvmOptions = (
    s"-agentlib:jdwp=transport=dt_socket" ::
    s"server=n" ::
    s"suspend=n" ::
    s"address=$address:$port" ::
    Nil).mkString(",")

  /**
   * Indicates whether or not the debugger is running.
   *
   * @return True if it is running, otherwise false
   */
  def isRunning: Boolean = components.nonEmpty

  /**
   * Indicates whether or not the listening debugger supports multiple JVM
   * connections or just a single JVM connecting.
   *
   * @return True if multiple JVMs can connect to this debugger, otherwise false
   */
  def supportsMultipleConnections: Boolean =
    findListeningConnector.exists(_.supportsMultipleConnections())

  /**
   * Starts the debugger, resulting in opening the specified socket to listen
   * for remote JVM connections.
   *
   * @param startProcessingEvents If true, events are immediately processed by
   *                              the VM as soon as it is connected
   * @param newVirtualMachineFunc The function to be invoked once per JVM that
   *                              connects to this debugger
   * @tparam T The return type of the callback function
   */
  def start[T](
    startProcessingEvents: Boolean,
    newVirtualMachineFunc: ScalaVirtualMachine => T
  ): Unit = {
    assert(!isRunning, "Debugger already started!")
    assertJdiLoaded()

    // Retrieve the listening connector, or throw an exception if failed
    val connector = findListeningConnector.getOrElse(
      throw new AssertionError("Unable to retrieve connector!"))

    val arguments = connector.defaultArguments()

    arguments.get("localAddress").setValue(address)
    arguments.get("port").setValue(port.toString)

    logger.info("Multiple Connections Allowed: " +
      connector.supportsMultipleConnections())

    // Open port for listening to JVM connections
    logger.info(s"Listening on $address:$port")
    connector.startListening(arguments)

    // Start the task runner to process JVMs
    logger.info("Starting looping task runner")
    loopingTaskRunner.start()

    // Store the connector and arguments (used for shutdown)
    components = Some((loopingTaskRunner, connector, arguments.asScala.toMap))

    // Start X workers to process connection requests
    logger.info(s"Spawning $workers worker tasks")
    (1 to workers).foreach(_ => loopingTaskRunner.addTask {
      listenTask(
        connector,
        arguments,
        startProcessingEvents,
        newVirtualMachineFunc
      )
    })
  }

  /**
   * Starts the debugger, resulting in opening the specified socket to listen
   * for remote JVM connections.
   *
   * @param newVirtualMachineFunc The function to be invoked once per JVM that
   *                              connects to this debugger
   * @tparam T The return type of the callback function
   */
  def start[T](newVirtualMachineFunc: ScalaVirtualMachine => T): Unit = {
    start(startProcessingEvents = true, newVirtualMachineFunc)
  }

  /**
   * Stops listening for incoming connections and shuts down the task runner.
   */
  def stop(): Unit = {
    assert(isRunning, "Debugger has not been started!")

    val (loopingTaskRunner, connector, arguments) = components.get

    // Close the listening port
    logger.info(s"Shutting down $address:$port")
    connector.stopListening(arguments.asJava)

    // Cancel all worker threads via interrupt
    logger.info("Cancelling worker threads")
    loopingTaskRunner.stop()

    // Mark that we have completely stopped the debugger
    components = None
  }

  /**
   * Retrieves the current listing of virtual machines that have connected to
   * this debugger.
   *
   * @return The collection of connected virtual machines
   */
  def connectedVirtualMachines =
    virtualMachineManager.connectedVirtualMachines().asScala.toSeq

  /**
   * Checks for an incoming connection, creates a new ScalaVirtualMachine for
   * the connection, and invokes the callback with the new ScalaVirtualMachine
   * instance.
   *
   * @param connector The connector to use when listening
   * @param arguments The arguments for the connector to use when accepting
   *                  new connections
   * @param startProcessingEvents If true, events are immediately processed by
   *                              the VM as soon as it is connected
   * @param newVirtualMachineFunc The callback for the new ScalaVirtualMachine
   * @tparam T The return type of the callback
   */
  protected def listenTask[T](
    connector: ListeningConnector,
    arguments: java.util.Map[String, Connector.Argument],
    startProcessingEvents: Boolean,
    newVirtualMachineFunc: ScalaVirtualMachine => T
  ): Unit = {
    val newVirtualMachine = Try(connector.accept(arguments))

    // Invoke our callback upon receiving a new virtual machine
    val scalaVirtualMachine = newVirtualMachine.map(newScalaVirtualMachine(
      _: VirtualMachine,
      newProfileManagerFunc(),
      loopingTaskRunner
    ))
    scalaVirtualMachine.foreach(_.initialize(
      startProcessingEvents = startProcessingEvents
    ))
    scalaVirtualMachine.foreach(newVirtualMachineFunc)

    // Release CPU
    Thread.sleep(1)
  }

  /**
   * Creates a new ScalaVirtualMachine instance.
   *
   * @param virtualMachine The underlying virtual machine
   * @param profileManager The profile manager associated with the
   *                       virtual machine
   * @param loopingTaskRunner The looping task runner used to process events
   *                          for the virtual machine
   *
   * @return The new ScalaVirtualMachine instance
   */
  protected def newScalaVirtualMachine(
    virtualMachine: VirtualMachine,
    profileManager: ProfileManager,
    loopingTaskRunner: LoopingTaskRunner
  ): ScalaVirtualMachine = new ScalaVirtualMachine(
    virtualMachine,
    profileManager,
    loopingTaskRunner
  )

  /**
   * Retrieves the connector to be used to listen for incoming JVM connections.
   *
   * @return Some connector if available, otherwise None
   */
  private def findListeningConnector: Option[ListeningConnector] = {
    virtualMachineManager.listeningConnectors().asScala
      .find(_.name() == ConnectorClassString)
  }
}
