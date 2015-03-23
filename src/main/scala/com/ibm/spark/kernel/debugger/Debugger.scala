package com.ibm.spark.kernel.debugger

import java.io.File
import java.net.{URL, URLClassLoader}

import com.ibm.spark.kernel.utils.LogLike
import com.sun.jdi.event._

import collection.JavaConverters._

import com.sun.jdi._

import scala.util.Try
import com.ibm.spark.kernel.utils.TryExtras.TryImplicits

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

  /**
   * Checks if the needed JDK exists on our classpath to use the Java debugger
   * interface.
   *
   * @param classLoader The class loader to use to check for JDI (default is
   *                    this class's class loader)
   *
   * @return True if JDI is able to be loaded, otherwise false
   */
  def isJdiAvailable(
    classLoader: ClassLoader = this.getClass.getClassLoader
  ): Boolean = {
    try {
      val rootJdiClass = "com.sun.jdi.Bootstrap"

      // Should throw an exception if JDI is not available
      Class.forName(rootJdiClass, false, classLoader)

      true
    } catch {
      case _: ClassNotFoundException  => false
      case ex: Throwable => throw ex
    }
  }

  /**
   * Attempts to ensure that the JDI is loaded. First, checks if the JDI is
   * already available. If not, attempts to find a JDK path and load it.
   *
   * @param classLoader The class loader to use to check for JDI (default is
   *                    this class's class loader)
   *
   * @return True if successful, otherwise false
   */
  def tryLoadJdi(
    classLoader: ClassLoader = this.getClass.getClassLoader
  ): Boolean = {
    // If the interface is available, quit early
    if (isJdiAvailable(classLoader)) return true

    // Report that we are going to have to "hackily" look around for the JDI
    logger.warn("JDI not found on classpath! Searching standard locations!")

    val baseLibDir = "lib"
    val neededJar = "tools.jar"
    val jarPath = s"$baseLibDir/$neededJar"

    // Get path to the Java installation being used to run this debugger
    val potentialJarPaths = {
      val paths = Seq(
        Try(System.getenv("JDK_HOME")),
        Try(System.getenv("JAVA_HOME")),
        Try(new File(System.getProperty("java.home")).getParent),
        Try(System.getProperty("java.home"))
      ).flatMap(_.toFilteredOption).map(_.trim).filter(_.nonEmpty)
        .map(_ + "/" + jarPath).distinct

      logger.trace(s"Found the following potential paths for $neededJar: " +
        paths.mkString(","))

      paths
    }

    // Lookup each path to see if the jar exists
    val validJarFiles = potentialJarPaths.map(new File(_)).filter(_.exists())

    // Attempt loading each jar and checking if JDI is available
    val validClassLoader = validJarFiles.map(jar => {
      logger.trace(s"Checking $jar for JDI")
      new URLClassLoader(Array(jar.toURI.toURL), classLoader)
    }).find(isJdiAvailable)

    // If there was no class loader that worked, exit
    if (validClassLoader.isEmpty) return false

    // Add the valid class loader to our system
    ClassLoader.getSystemClassLoader match {
      case urlClassLoader: URLClassLoader =>
        val validJarUrl = validClassLoader.get.getURLs.head

        // Add the jar to our system class loader
        val addUrlMethod = {
          val method =
            classOf[URLClassLoader].getDeclaredMethod("addURL", classOf[URL])
          method.setAccessible(true)

          method
        }

        logger.info(s"Using ${validJarUrl.getFile} for JDI")
        addUrlMethod.invoke(urlClassLoader, validJarUrl)

        // Final check to ensure that it was loaded
        isJdiAvailable()

      case _ =>
        logger.warn(
          """
            |Found valid tools.jar, but unable to add to system class loader as
            |it is not an instance of a URL class loader!
          """.stripMargin.replace("\n", " "))
        false
    }
  }

  def start(): Unit = {
    require(tryLoadJdi(),
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
    })

    // Ensure that we start processing first to avoid missing some event
    // when receiving a connection???
    processingThread.start()
    connectionThread.start()
  }

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
