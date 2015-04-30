package com.senkbeil.debugger

import com.senkbeil.Main
import com.senkbeil.debugger.events.LoopingTaskRunner
import com.senkbeil.debugger.jdi.JDILoader
import com.senkbeil.utils.LogLike
import com.senkbeil.debugger.wrappers._
import com.sun.jdi.event._

import collection.JavaConverters._

import com.sun.jdi._

import scala.util.Try

/**
 * Represents the main entrypoint for the debugger against the internal
 * interpreters and the Spark cluster.
 * @param address The address to use for remote JVMs to attach to this debugger
 * @param port The port to use for remote JVMs to attach to this debugger
 */
class ListeningDebugger(address: String, port: Int)
  extends Debugger with LogLike
{
  private val ConnectorClassString = "com.sun.jdi.SocketListen"
  private val loopingTaskRunner = new LoopingTaskRunner()
  @volatile private var virtualMachines =
    List[(VirtualMachine, ScalaVirtualMachine)]()

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
   * Starts the debugger.
   */
  def start(): Unit = {
    require(jdiLoader.tryLoadJdi(),
      """
        |Unable to load Java Debugger Interface! This is part of tools.jar
        |provided by OpenJDK/Oracle JDK and is the core of the debugger! Please
        |make sure that JAVA_HOME has been set and that tools.jar is available
        |on the classpath!
      """.stripMargin.replace("\n", " "))

    val connector = getConnector.getOrElse(throw new IllegalArgumentException)

    val arguments = connector.defaultArguments()

    arguments.get("localAddress").setValue(address)
    arguments.get("port").setValue(port.toString)

    logger.info("Multiple Connections Allowed: " +
      connector.supportsMultipleConnections())

    connector.startListening(arguments)

    // Virtual machine connection thread
    val connectionThread = new Thread(new Runnable {
      override def run(): Unit = while (true) try {
        val newVirtualMachine = Try(connector.accept(arguments))
        newVirtualMachine
          .map(virtualMachine => (
            virtualMachine,
            new ScalaVirtualMachine(virtualMachine, loopingTaskRunner)
          )).foreach(virtualMachines +:= _)

        val (virtualMachine, scalaVirtualMachine) = virtualMachines.last

        val virtualMachineName = virtualMachine.name()
        scalaVirtualMachine.eventManager
          .addEventHandler(classOf[VMStartEvent], (ev) => {
          println("CONNECTED!!!")
          logger.debug(s"($virtualMachineName) Connected!")

          // Sometimes this event is not triggered! Need to do this
          // request outside of this event, maybe? Or just not know
          // which executor is being matched...

          // NOTE: If this succeeds, we get an extra argument which IS
          // the main executing class name! Is there a way to guarantee
          // that this is executed? Should we just assume it will be?
          //Debugger.printCommandLineArguments(virtualMachine)
          println("ARGS: " +
            scalaVirtualMachine.commandLineArguments.mkString(","))
        })

        scalaVirtualMachine.eventManager.addEventHandler(classOf[VMDisconnectEvent], (_) => {
          logger.debug(s"($virtualMachineName) Disconnected!")
          virtualMachines = virtualMachines diff List(virtualMachine)
        })

        scalaVirtualMachine.eventManager.addEventHandler(classOf[ClassPrepareEvent], (e) => {
          val ev = e.asInstanceOf[ClassPrepareEvent]
          logger.debug(s"($virtualMachineName) New class: ${ev.referenceType().name()}")
        })

        scalaVirtualMachine.eventManager.addEventHandler(classOf[BreakpointEvent], (e) => {
          val ev = e.asInstanceOf[BreakpointEvent]
          logger.debug(s"Hit breakpoint at location: ${ev.location()}")

          println("<FRAME>")
          println("THREAD STATUS: " + ev.thread().status())
          val stackFrame = ev.thread().frames().asScala.head
          Try({
            val location = stackFrame.location()
            println(s"${location.sourceName()}:${location.lineNumber()}")
          })

          println("<FRAME OBJECT VARIABLES>")
          stackFrame.thisVisibleFieldMap().foreach {
            case (field, value) => Try(println(
              field.name() + ": " + value.toString(2)
            ))
          }

          println("<FRAME LOCAL VARIABLES>")
          stackFrame.localVisibleVariableMap().foreach {
            case (localVariable, value) => Try(println(
              localVariable.name() + ": " + value.toString
            ))
          }

          println()

          /*while ({ print("Continue(y/n): "); Console.in.readLine() } != "y") {
            Thread.sleep(1)
          }*/

          scalaVirtualMachine.breakpointManager
            .removeLineBreakpoint(Main.testMainFile, 42)
        })

        // Give resources back to CPU
        Thread.sleep(1)
      } catch {
        case ex: Exception => ex.printStackTrace()
      }
    })

    connectionThread.start()
  }

  def stop(): Unit = {}

  /**
   * Retrieves the current listing of virtual machines that have connected to
   * this debugger.
   *
   * @return The list of virtual machines
   */
  def getVirtualMachines = synchronized { virtualMachines }

  /*private val updateVirtualMachines =
    (connector: ListeningConnector, arguments: Map[String, Connector.Argument]) => {
      val newVirtualMachine = Try(connector.accept(arguments.asJava))
      newVirtualMachine.foreach(virtualMachines.add)
    }*/

  private def getConnector = {
    val virtualMachineManager = Bootstrap.virtualMachineManager()

    virtualMachineManager.listeningConnectors().asScala
      .find(_.name() == ConnectorClassString)
  }
}
