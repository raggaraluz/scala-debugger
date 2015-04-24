package com.ibm.spark.kernel.debugger

import com.ibm.spark.kernel.Main
import com.ibm.spark.kernel.utils.LogLike
import com.sun.jdi.event._

import collection.JavaConverters._

import com.sun.jdi._

import scala.util.Try

import com.ibm.spark.kernel.debugger.wrapper._

/**
 * Represents the main entrypoint for the debugger against the internal
 * interpreters and the Spark cluster.
 * @param address The address to use for remote JVMs to attach to this debugger
 * @param port The port to use for remote JVMs to attach to this debugger
 */
class Debugger(address: String, port: Int) extends LogLike {
  private val ConnectorClassString = "com.sun.jdi.SocketListen"
  @volatile private var virtualMachines =
    List[(VirtualMachine, ScalaVirtualMachine)]()
  private val jdiLoader = new JDILoader(this.getClass.getClassLoader)

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
   * Determines whether or not the debugger is available for use.
   *
   * @return True if the debugger is available, otherwise false
   */
  def isAvailable: Boolean = jdiLoader.isJdiAvailable()

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

    println("MULTIPLE CONNECTIONS ALLOWED: " +
      connector.supportsMultipleConnections())

    connector.startListening(arguments)

    // Virtual machine connection thread
    val connectionThread = new Thread(new Runnable {
      override def run(): Unit = while (true) try {
        val newVirtualMachine = Try(connector.accept(arguments))
        newVirtualMachine
          .map(virtualMachine =>
            (virtualMachine, new ScalaVirtualMachine(virtualMachine))
          ).foreach(virtualMachines +:= _)

        // Give resources back to CPU
        Thread.sleep(1)
      } catch {
        case ex: Exception => ex.printStackTrace()
      }
    })

    // Event processing thread
    val processingThread = new Thread(new Runnable {
      override def run(): Unit = while (true) try {
        virtualMachines.foreach { case (virtualMachine, scalaVirtualMachine) =>
          val virtualMachineName = virtualMachine.name()
          val eventQueue = virtualMachine.eventQueue()
          val eventSet = eventQueue.remove()
          val eventSetIterator = eventSet.iterator()
          while (eventSetIterator.hasNext) {
            val event = eventSetIterator.next()
            event match {
              case ev: VMStartEvent =>
                logger.debug(s"($virtualMachineName) Connected!")

                // Sometimes this event is not triggered! Need to do this
                // request outside of this event, maybe? Or just not know
                // which executor is being matched...

                // NOTE: If this succeeds, we get an extra argument which IS
                // the main executing class name! Is there a way to guarantee
                // that this is executed? Should we just assume it will be?
                //Debugger.printCommandLineArguments(virtualMachine)
                scalaVirtualMachine.commandLineArguments.foreach(arg =>
                  println("ARG: " + arg)
                )

                eventSet.resume()
              case _: VMDisconnectEvent =>
                logger.debug(s"($virtualMachineName) Disconnected!")
                virtualMachines = virtualMachines diff List(virtualMachine)
                eventSet.resume()
              case ev: ClassPrepareEvent =>
                logger.debug(s"($virtualMachineName) New class: ${ev.referenceType().name()}")
                eventSet.resume()
              case ev: BreakpointEvent =>
                logger.debug(s"Hit breakpoint at location: ${ev.location()}")

                println("<FRAME>")
                println("THREAD STATUS: " + ev.thread().status())
                val stackFrame = ev.thread().frames().asScala.head
                Try({
                  val location = stackFrame.location()
                  println(s"${location.sourceName()}:${location.lineNumber()}")
                })

                println("<FRAME OBJECT VARIABLES>")
                stackFrame.scalaThisVisibleFieldMap().foreach {
                  case (field, value) => Try(println(
                    field.name() + ": " + value.toString(2)
                  ))
                }

                println("<FRAME LOCAL VARIABLES>")
                stackFrame.scalaLocalVisibleVariableMap().foreach {
                  case (localVariable, value) => Try(println(
                    localVariable.name() + ": " + value.toString
                  ))
                }

                println()

                while ({print("Continue(y/n): "); Console.readLine()} != "y") {
                  Thread.sleep(1)
                }

                scalaVirtualMachine.breakpointManager
                  .removeLineBreakpoint(Main.testMainClass, 13)

                eventSet.resume()
              case ev: Event => // Log unhandled event
                logger.warn(s"Not handling event: ${ev.toString}")

              case _ => // Ignore other events
            }
          }
        }

        // Give resources back to CPU
        Thread.sleep(1)
      } catch {
        case ex: Exception => ex.printStackTrace()
      }
    })

    // Ensure that we start processing first to avoid missing some event
    // when receiving a connection???
    processingThread.start()
    connectionThread.start()
  }

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
