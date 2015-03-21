package com.ibm.spark.kernel.debugger

import java.util
import java.util.concurrent.atomic.AtomicBoolean

import com.ibm.spark.kernel.utils.LogLike
import com.sun.jdi.event._

import collection.JavaConverters._

import com.sun.jdi._

import scala.util.Try

object Debugger {

  // TODO: Use this to get the executor id from the forked executors in Spark
  // E.g. CoarseGrainedExecutorBackend <driverUrl> <executorId> <hostname>
  //                                   <cores> <appid> [<workerUrl>]
  def printCommandLineArguments(virtualMachine: VirtualMachine) = {
    def printArguments(values: Seq[Value]): Unit = {
      values.foreach {
        case arrayReference: ArrayReference =>
          printArguments(arrayReference.getValues.asScala)
        case stringReference: StringReference =>
          println("ARG: " + stringReference.value())
        case objectReference: ObjectReference =>
          println("CLASS: " + objectReference.referenceType().name())
        case v => // Ignore any other values (some show up due to Scala)
          println("ARG: " + v)
      }
    }

    // Get the main thread of execution
    val mainThread = virtualMachine.allThreads().asScala
      .find(_.name() == "main").get

    // Print out command line arguments for connected JVM
    virtualMachine.suspend()
    mainThread.suspend()
    println("===MAIN===")
    mainThread.frames().asScala
      .find(_.location().method().name() == "main")
      .map(stackFrame => {
      val stackFrameArgumentValues =
        stackFrame.getArgumentValues.asScala.toSeq
      stackFrameArgumentValues
    }).foreach(printArguments)
    println("===MEND===")
    mainThread.resume()
    virtualMachine.resume()
  }
}

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

  def getVirtualMachines = synchronized { virtualMachines }

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

    // Virtual machine connection thread
    new Thread(new Runnable {
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
    }).start()

    // Event processing thread
    new Thread(new Runnable {
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
                Debugger.printCommandLineArguments(virtualMachine)

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
                val stackFrame = ev.thread().frames().asScala.head
                Try({
                  val location = stackFrame.location()
                  println(s"${location.sourceName()}:${location.lineNumber()}")
                })

                def printFieldValue(field:Field, value: Value, maxRecursion: Int = 0, currentRecursion: Int = 1): Unit = {
                  if (maxRecursion > 0 && currentRecursion > maxRecursion)
                    return

                  print(s"${field.name()} == ")
                  value match {
                    case booleanValue: BooleanValue =>
                      println(booleanValue.value())
                    case byteValue: ByteValue =>
                      println(byteValue.value())
                    case charValue: CharValue =>
                      println(charValue.value())
                    case doubleValue: DoubleValue =>
                      println(doubleValue.value())
                    case floatValue: FloatValue =>
                      println(floatValue.value())
                    case integerValue: IntegerValue =>
                      println(integerValue.value())
                    case longValue: LongValue =>
                      println(longValue.value())
                    case shortValue: ShortValue =>
                      println(shortValue.value())
                    case primitiveValue: PrimitiveValue =>
                      println("Unknown primitive: " + primitiveValue)
                      //throw new RuntimeException("Unknown primitive: " + primitiveValue)
                    case objectReference: ObjectReference =>
                      println(objectReference)
                      objectReference.referenceType().visibleFields().asScala
                        .map(field => Try((field, objectReference.getValue(field))).getOrElse((field, null)))
                        .filterNot(_._2 == null)
                        .filterNot(_._2.eq(objectReference))
                        .foreach(t => printFieldValue(t._1, t._2, maxRecursion, currentRecursion + 1))
                    case _ =>
                      println("Unknown value: " + value)
                      //throw new RuntimeException("Unknown value: " + value)
                  }
                }

                println("<FRAME OBJECT VARIABLES>")
                val stackObject = stackFrame.thisObject()
                val stackObjectReferenceType = stackObject.referenceType()
                val fieldsAndValues = stackObjectReferenceType.visibleFields().asScala.map { field =>
                  (field, stackObject.getValue(field))
                }
                fieldsAndValues.foreach { case (field, value) =>
                    printFieldValue(field, value, maxRecursion = 2)
                }

                println("<FRAME LOCAL VARIABLES>")
                Try(stackFrame.visibleVariables())
                  .map(_.asScala).getOrElse(Nil)
                  .filterNot(_.isArgument)
                  .foreach { localVariable =>
                  Try(println(localVariable))
                }

                /*ev.thread().frames().asScala.foreach { stackFrame =>
                  Try({
                    val location = stackFrame.location()
                    println(s"${location.sourceName()}:${location.lineNumber()}")
                  })

                  Try(stackFrame.visibleVariables())
                    .map(_.asScala).getOrElse(Nil)
                    .filterNot(_.isArgument)
                    .foreach { localVariable =>
                      Try(println(localVariable.signature()))
                    }
                }*/

                println()

                while ({print("Continue(y/n): "); Console.readLine()} != "y") {
                  Thread.sleep(1)
                }

                scalaVirtualMachine.breakpointManager
                  .removeLineBreakpoint("DummyMain", 13)

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
    }).start()
  }

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
