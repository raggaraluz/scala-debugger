package com.ibm.spark.kernel.debugger

import com.ibm.spark.kernel.utils.LogLike
import com.sun.jdi._

class ScalaVirtualMachine(protected val _virtualMachine: VirtualMachine)
  extends JDIHelperMethods with LogLike
{
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
}

