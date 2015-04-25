package com.ibm.spark.kernel.debugger

import com.ibm.spark.kernel.utils.LogLike
import com.sun.jdi._
import collection.JavaConverters._

class ScalaVirtualMachine(protected val _virtualMachine: VirtualMachine)
    extends JDIHelperMethods with LogLike {
  val classManager = new ClassManager(_virtualMachine)
  val breakpointManager = new BreakpointManager(_virtualMachine, classManager)
  val fieldManager = new FieldManager(_virtualMachine, classManager)

  /**
   * Retrieves the list of available lines for a specific class.
   *
   * @param className The name of the class whose lines to retrieve
   *
   * @return The list of breakpointable lines
   */
  def availableLinesForClass(className: String): Seq[Int] =
    classManager.linesAndLocationsForClass(className).keys.toSeq.sorted

  /**
   * Represents the fully-qualified class name that invoked the main method of
   * this virtual machine.
   *
   * @return The name as a string
   */
  lazy val mainClassName: String = {
    val mainThread = findMainThread()

    val tryClassName = suspendVirtualMachineAndExecute {
      val mainMethodFrame = mainThread.frames().asScala
        .find(_.location().method().name() == "main")

      assert(mainMethodFrame.nonEmpty, "Error locating main method!")

      mainMethodFrame.get.location()
    }.map(_.declaringType().name())

    // Throw our exception if we get one
    if (tryClassName.isFailure) tryClassName.failed.foreach(ex => throw ex)

    // Return the resulting class name
    tryClassName.get
  }

  /**
   * Represents the command line arguments used to start this virtual machine.
   *
   * @return The sequence of arguments as strings
   */
  lazy val commandLineArguments: Seq[String] = {
    def processArguments(values: Seq[Value]): Seq[String] = {
      values.flatMap {
        // Should represent the whole array of string arguments, drill down
        case arrayReference: ArrayReference =>
          processArguments(arrayReference.getValues.asScala)

        // Base structure (string) should be returned as an argument
        case stringReference: StringReference =>
          Seq(stringReference.value())

        // NOTE: A reference to the underlying class tends to show up as an
        // additional value after the virtual machine is initialized, so we
        // want to ignore it without flooding our logging output
        case objectReference: ObjectReference => Nil

        // Ignore any other values (some show up due to Scala)
        case v =>
          logger.warn("Unknown value during processing arguments: " + v)
          Nil
      }
    }

    // Get the main thread of execution
    val mainThread = findMainThread()

    // Print out command line arguments for connected JVM
    suspendVirtualMachineAndExecute {
      mainThread.suspend()

      val arguments = mainThread.frames().asScala
        .find(_.location().method().name() == "main")
        .map(_.getArgumentValues.asScala.toSeq)
        .map(processArguments)
        .getOrElse(Nil)

      mainThread.resume()

      arguments
    }.getOrElse(Nil)
  }
}

