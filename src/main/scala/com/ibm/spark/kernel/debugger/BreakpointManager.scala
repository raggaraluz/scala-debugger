package com.ibm.spark.kernel.debugger

import com.ibm.spark.kernel.utils.LogLike
import com.sun.jdi.VirtualMachine
import com.sun.jdi.request.{EventRequest, BreakpointRequest}

class BreakpointManager(
  protected val _virtualMachine: VirtualMachine,
  private val _classManager: ClassManager
) extends JDIHelperMethods with LogLike {
  private val eventRequestManager = _virtualMachine.eventRequestManager()

  type BreakpointBundleKey = (String, Int) // Class Name, Line Number
  private var lineBreakpoints = Map[BreakpointBundleKey, BreakpointBundle]()

  /**
   * Retrieves the list of breakpoints contained by this manager.
   *
   * @return The collection of breakpoints in the form of
   *         (class name, line number)
   */
  def breakpointList: Seq[BreakpointBundleKey] = lineBreakpoints.keys.toSeq

  /**
   * Creates and enables a breakpoint on the specified line of the class.
   *
   * @param className The name of the class to set a breakpoint
   * @param lineNumber The number of the line to break
   * @param enabled If true, enables the breakpoint (default is true)
   * @param suspendPolicy Indicates the policy for suspending when the
   *                      breakpoint is hit (default is all threads)
   *
   * @return True if successfully added breakpoints, otherwise false
   */
  def setLineBreakpoint(
    className: String,
    lineNumber: Int,
    enabled: Boolean = true,
    suspendPolicy: Int = EventRequest.SUSPEND_ALL
  ): Boolean = {
    // Retrieve the available locations for the specified line
    val locations = {
      val linesAndLocations = _classManager.linesAndLocationsForClass(className)
      require(linesAndLocations.contains(lineNumber),
        s"$lineNumber is not an available line for $className!")

      linesAndLocations(lineNumber)
    }

    // Create and enable breakpoints for all underlying locations
    val result = suspendVirtualMachineAndExecute {
      // Our key is using the class name and line number relevant to the
      // line breakpoint
      val key: BreakpointBundleKey = (className, lineNumber)

      // Build our bundle of breakpoints
      val breakpointBundle = new BreakpointBundle(
        locations.map(eventRequestManager.createBreakpointRequest)
      )

      // Set relevant information over all breakpoints
      breakpointBundle.setSuspendPolicy(suspendPolicy)
      breakpointBundle.setEnabled(enabled)

      // Add the bundle to our list of line breakpoints
      lineBreakpoints += key -> breakpointBundle
    }

    // Log the error if one occurred
    if (result.isFailure) logger.throwable(result.failed.get)

    // Log if successful
    if (result.isSuccess)
      logger.trace(s"Added breakpoint $className:$lineNumber")

    result.isSuccess
  }

  /**
   * Determines whether or not the breakpoint for the specific class's line.
   *
   * @param className The name of the class whose line to reference
   * @param lineNumber The number of the line to check for a breakpoint
   *
   * @return True if a breakpoint exists, otherwise false
   */
  def hasLineBreakpoint(className: String, lineNumber: Int): Boolean =
    lineBreakpoints.contains((className, lineNumber))

  /**
   * Returns the bundle of breakpoints representing the breakpoint for the
   * specified line.
   *
   * @param className The name of the class whose line to reference
   * @param lineNumber The number of the line to check for breakpoints
   *
   * @return The bundle of breakpoints for the specified line, or an error if
   *         the specified line has no breakpoints
   */
  def getLineBreakpoint(className: String, lineNumber: Int): BreakpointBundle =
    lineBreakpoints((className, lineNumber))

  /**
   * Removes the breakpoint on the specified line of the class.
   *
   * @param className The name of the class to remove the breakpoint
   * @param lineNumber The number of the line to break
   *
   * @return True if successfully removed breakpoint, otherwise false
   */
  def removeLineBreakpoint(className: String, lineNumber: Int): Boolean = {
    // Remove breakpoints for all underlying locations
    val result = suspendVirtualMachineAndExecute {
      val key: BreakpointBundleKey = (className, lineNumber)

      val breakpointBundleToRemove = lineBreakpoints(key)

      lineBreakpoints -= key

      breakpointBundleToRemove.foreach(eventRequestManager.deleteEventRequest)
    }

    // Log the error if one occurred
    if (result.isFailure) logger.throwable(result.failed.get)

    // Log if successful
    if (result.isSuccess)
      logger.trace(s"Removed breakpoint $className:$lineNumber")

    result.isSuccess
  }
}
