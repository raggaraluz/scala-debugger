package com.ibm.spark.kernel.debugger

import java.util

import com.sun.jdi.event._

import collection.JavaConverters._

import com.sun.jdi.{VirtualMachine, Bootstrap}

import scala.util.Try

/**
 * Represents the main entrypoint for the debugger against the internal
 * interpreters and the Spark cluster.
 * @param address The address to use for remote JVMs to attach to this debugger
 * @param port The port to use for remote JVMs to attach to this debugger
 */
class Debugger(address: String, port: Int) {
  private val ConnectorClassString = "com.sun.jdi.SocketListen"
  private val virtualMachines = new util.Vector[VirtualMachine]

  /**
   * Represents the JVM options to feed to remote JVMs whom will connect to
   * this debugger.
   */
  val RemoteJvmOptions = (
    s"-agentlib:jdwp=transport=dt_socket" ::
      s"server=n" ::
      s"suspend=n" ::
      s"address=$address:$port" ::
      Nil).mkString(",")

  def start(): Unit = {
    val connector = getConnector.getOrElse(throw new IllegalArgumentException)

    val arguments = connector.defaultArguments()

    arguments.get("localAddress").setValue(address)
    arguments.get("port").setValue(port.toString)

    connector.startListening(arguments)

    new Thread(new Runnable {
      override def run(): Unit = while (true) try {
        val newVirtualMachine = Try(connector.accept(arguments))
        newVirtualMachine.foreach(virtualMachines.add)

        virtualMachines.asScala.foreach { virtualMachine =>
          val virtualMachineName = virtualMachine.name()
          val eventQueue = virtualMachine.eventQueue()
          val eventSet = eventQueue.remove()
          val eventSetIterator = eventSet.iterator()
          while (eventSetIterator.hasNext) {
            val event = eventSetIterator.next()
            event match {
              case _: VMStartEvent =>
                println(s"($virtualMachineName) Connected!")
                eventSet.resume()
              case _: VMDisconnectEvent =>
                println(s"($virtualMachineName) Disconnected!")
                virtualMachines.remove(virtualMachine)
                eventSet.resume()
              case ev: ClassPrepareEvent =>
                println(s"($virtualMachineName) New class: ${ev.referenceType().name()}")
                eventSet.resume()
              case ev: BreakpointEvent =>
                println(s"Hit breakpoint at location: ${ev.location()}")
                eventSet.resume()
              case ev: Event => // Log unhandled event
                println(s"Not handling event: ${ev.toString}")
              case _ => // Ignore other events
            }
          }
        }
        Thread.sleep(1)
      } catch {
        case ex: Exception => ex.printStackTrace()
      }
    }).start()
  }

  def getVirtualMachines = synchronized { virtualMachines.asScala }

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
