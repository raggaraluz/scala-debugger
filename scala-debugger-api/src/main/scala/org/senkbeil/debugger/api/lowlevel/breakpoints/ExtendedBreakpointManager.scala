package org.senkbeil.debugger.api.lowlevel.breakpoints

import com.sun.jdi.request.BreakpointRequest
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.utils.PendingActionManager

import scala.util.{Success, Try}

/**
 * Represents a breakpoint manager that has been extended to support pending
 * breakpoints.
 *
 * @param breakpointManager The breakpoint manager to extend with
 *                          pending breakpoint support
 * @param pendingActionManager The manager used to store pending breakpoints
 *                             and process them later
 */
class ExtendedBreakpointManager(
  private val breakpointManager: BreakpointManager,
  private val pendingActionManager: PendingActionManager[BreakpointRequestInfo] =
    new PendingActionManager[BreakpointRequestInfo]
) extends BreakpointManager {

  /**
   * Processes all pending breakpoints.
   *
   * @return The collection of successfully-processed breakpoints
   */
  def processAllPendingBreakpoints(): Seq[BreakpointRequestInfo] = {
    pendingActionManager.processAllActions().map(_.data)
  }

  /**
   * Processes all pending breakpoints for the specified file.
   *
   * @param fileName The name of the file whose pending breakpoints to process
   *
   * @return The collection of successfully-processed breakpoints
   */
  def processPendingBreakpointsForFile(
    fileName: String
  ): Seq[BreakpointRequestInfo] = {
    pendingActionManager.processActions(_.data.fileName == fileName).map(_.data)
  }

  /**
   * Retrieves a list of pending breakpoints for the specified file.
   *
   * @param fileName The name of the file whose pending breakpoints to retrieve
   *
   * @return The collection of successfully-processed breakpoints
   */
  def pendingBreakpointsForFile(fileName: String): Seq[BreakpointRequestInfo] = {
    pendingActionManager.getPendingActionData(_.data.fileName == fileName)
  }

  /**
   * Removes the breakpoint on the specified line of the file. Will also remove
   * any pending breakpoints.
   *
   * @param fileName The name of the file to remove the breakpoint
   * @param lineNumber The number of the line to break
   *
   * @return True if successfully removed breakpoint, otherwise false
   */
  override def removeBreakpointRequest(
    fileName: String,
    lineNumber: Int
  ): Boolean = {
    val result = breakpointManager.removeBreakpointRequest(
      fileName,
      lineNumber
    )

    val pendingResult = pendingActionManager.removePendingActions(a =>
      a.data.fileName == fileName && a.data.lineNumber == lineNumber
    )

    // True if we removed a real breakpoint or any pending breakpoints
    result || pendingResult.nonEmpty
  }

  /**
   * Removes the breakpoint on the specified line of the file. Will also remove
   * any pending breakpoints.
   *
   * @param requestId The id of the request to remove
   *
   * @return True if successfully removed breakpoint, otherwise false
   */
  override def removeBreakpointRequestWithId(
    requestId: String
  ): Boolean = {
    val result = breakpointManager.removeBreakpointRequestWithId(requestId)

    val pendingResult = pendingActionManager.removePendingActionsWithId(
      requestId
    )

    // True if we removed a real breakpoint or any pending breakpoints
    result || pendingResult.getOrElse(Nil).nonEmpty
  }

  /**
   * Creates and enables a breakpoint on the specified line of the class.
   *
   * @param fileName The name of the file to set a breakpoint
   * @param lineNumber The number of the line to break
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful or pending, otherwise Failure
   */
  override def createBreakpointRequest(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIRequestArgument*
  ): Try[String] = createBreakpointRequestWithId(
    newRequestId(),
    fileName,
    lineNumber,
    extraArguments: _*
  )

  /**
   * Creates and enables a breakpoint on the specified line of the class. Will
   * also remove any pending breakpoints.
   *
   * @param requestId The id of the request used for lookup and removal
   * @param fileName The name of the file to set a breakpoint
   * @param lineNumber The number of the line to break
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful or pending, otherwise Failure
   */
  override def createBreakpointRequestWithId(
    requestId: String,
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    def createBreakpoint() = breakpointManager.createBreakpointRequestWithId(
      requestId,
      fileName,
      lineNumber,
      extraArguments: _*
    )

    val result = createBreakpoint()

    // If succeeded in adding the breakpoint, exit early
    if (result.isSuccess) return result

    result.recoverWith {
      case _: NoBreakpointLocationFound =>
        pendingActionManager.addPendingActionWithId(
          requestId,
          BreakpointRequestInfo(fileName, lineNumber, extraArguments),
          () => createBreakpoint().get
        )
        Success(requestId)
      case _: Throwable => result
    }
  }

  /**
   * Retrieves the list of breakpoints contained by this manager.
   *
   * @return The collection of breakpoints in the form of information
   */
  override def breakpointRequestList: Seq[BreakpointRequestInfo] =
    breakpointManager.breakpointRequestList

  /**
   * Returns the collection of breakpoints with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return Some collection of breakpoints for the specified line, or None if
   *         the specified line has no breakpoints
   */
  override def getBreakpointRequestWithId(
    requestId: String
  ): Option[Seq[BreakpointRequest]] =
    breakpointManager.getBreakpointRequestWithId(requestId)

  /**
   * Retrieves the list of breakpoints contained by this manager.
   *
   * @return The collection of breakpoints by id
   */
  override def breakpointRequestListById: Seq[String] =
    breakpointManager.breakpointRequestListById

  /**
   * Determines whether or not the breakpoint for the specific file's line.
   *
   * @param fileName The name of the file whose line to reference
   * @param lineNumber The number of the line to check for a breakpoint
   *
   * @return True if a breakpoint exists, otherwise false
   */
  override def hasBreakpointRequest(
    fileName: String,
    lineNumber: Int
  ): Boolean = breakpointManager.hasBreakpointRequest(fileName, lineNumber)

  /**
   * Returns the arguments for a breakpoint request with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return Some breakpoint arguments if found, otherwise None
   */
  override def getBreakpointRequestInfoWithId(
    requestId: String
  ): Option[BreakpointRequestInfo] =
    breakpointManager.getBreakpointRequestInfoWithId(requestId)

  /**
   * Returns the collection of breakpoints representing the breakpoint for the
   * specified line.
   *
   * @param fileName The name of the file whose line to reference
   * @param lineNumber The number of the line to check for breakpoints
   *
   * @return Some collection of breakpoints for the specified line, or None if
   *         the specified line has no breakpoints
   */
  override def getBreakpointRequest(
    fileName: String,
    lineNumber: Int
  ): Option[Seq[BreakpointRequest]] =
    breakpointManager.getBreakpointRequest(fileName, lineNumber)

  /**
   * Determines whether or not the breakpoint with the specified id exists.
   *
   * @param requestId The id of the request
   *
   * @return True if a breakpoint exists, otherwise false
   */
  override def hasBreakpointRequestWithId(requestId: String): Boolean =
    breakpointManager.hasBreakpointRequestWithId(requestId)
}
