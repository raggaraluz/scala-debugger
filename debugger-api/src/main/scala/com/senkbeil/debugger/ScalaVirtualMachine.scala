package com.senkbeil.debugger

import com.senkbeil.debugger.breakpoints.BreakpointManager
import com.senkbeil.debugger.classes.ClassManager
import com.senkbeil.debugger.events.{LoopingTaskRunner, EventManager}
import com.senkbeil.debugger.jdi.JDIHelperMethods
import com.senkbeil.utils.LogLike
import com.sun.jdi._
import collection.JavaConverters._

/**
 * Represents a virtual machine running Scala code.
 *
 * @param _virtualMachine The underlying virtual machine
 * @param loopingTaskRunner The runner used to process events
 */
class ScalaVirtualMachine(
  protected val _virtualMachine: VirtualMachine,
  private val loopingTaskRunner: LoopingTaskRunner
) extends JDIHelperMethods with LogLike {
  // Lazily-load the class manager (and as a result, the other managers) to
  // give enough time to retrieve all of the classes
  lazy val classManager =
    new ClassManager(_virtualMachine, loadClasses = true)
  lazy val breakpointManager =
    new BreakpointManager(_virtualMachine, classManager)
  lazy val eventManager =
    new EventManager(_virtualMachine, loopingTaskRunner)

  /**
   * Retrieves the list of available lines for a specific file.
   *
   * @param fileName The name of the file whose lines to retrieve
   *
   * @return Some list of breakpointable lines if the file exists, otherwise
   *         None
   */
  def availableLinesForFile(fileName: String): Option[Seq[Int]] =
    classManager.linesAndLocationsForFile(fileName).map(_.keys.toSeq.sorted)

  /**
   * Represents the fully-qualified class name that invoked the main method of
   * this virtual machine.
   *
   * @return The name as a string
   */
  lazy val mainClassName: String = {
    val mainThread = findMainThread().get

    // TODO: Investigate if necessary to suspend entire virtual machine or
    //       just the main thread
    val tryClassName = suspendVirtualMachineAndExecute {
      val mainMethodFrame = mainThread.frames().asScala
        .map(_.location()).find(_.method().name() == "main")

      assert(mainMethodFrame.nonEmpty, "Error locating main method!")

      mainMethodFrame.get
    }.map(_.declaringType().name())

    // Throw our exception if we get one
    tryClassName.failed.foreach(ex => throw ex)

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
    val mainThread = findMainThread().get

    // TODO: Investigate if necessary to suspend entire virtual machine or
    //       just the main thread
    // Retrieve command line arguments for connected JVM
    val tryArguments = suspendVirtualMachineAndExecute {
      suspendThreadAndExecute(mainThread) {
        val arguments = mainThread.frames().asScala
          .find(_.location().method().name() == "main")
          .map(_.getArgumentValues.asScala.toSeq)
          .map(processArguments)

        assert(arguments.nonEmpty, "Error locating main method!")

        arguments.get
      }
    }.flatten

    // Throw our exception if we get one
    tryArguments.failed.foreach(ex => throw ex)

    // Return the resulting arguments
    tryArguments.get
  }
}

