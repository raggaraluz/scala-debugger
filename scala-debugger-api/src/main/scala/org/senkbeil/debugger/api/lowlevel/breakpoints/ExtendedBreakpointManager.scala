package org.senkbeil.debugger.api.lowlevel.breakpoints

import com.sun.jdi.request.EventRequestManager
import org.senkbeil.debugger.api.lowlevel.classes.ClassManager
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.utils.PendingActionManager

import scala.util.Try

/**
 * Represents a breakpoint manager that has been extended to support pending
 * breakpoints.
 *
 * @param eventRequestManager The manager used to create breakpoint requests
 * @param classManager The class manager associated with the virtual machine,
 *                      used to retrieve location information
 * @param pendingActionManager The manager used to store pending breakpoints
 *                             and process them later
 */
class ExtendedBreakpointManager(
  private val eventRequestManager: EventRequestManager,
  private val classManager: ClassManager,
  private val pendingActionManager: PendingActionManager[BreakpointInfo] =
    new PendingActionManager[BreakpointInfo]
) extends BreakpointManager(eventRequestManager, classManager) {

  /**
   * Processes all pending breakpoints for the specified file.
   *
   * @param fileName The name of the file whose pending breakpoints to process
   *
   * @return The collection of processed breakpoints
   */
  def processPendingBreakpointsForFile(
    fileName: String
  ): Seq[BreakpointInfo] = {
    pendingActionManager.processActions(_.data.fileName == fileName).map(_.data)
  }

  /**
   * Retrieves a list of pending breakpoints for the specified file.
   *
   * @param fileName The name of the file whose pending breakpoints to retrieve
   *
   * @return The collection of pending breakpoints in the form of
   *         (class name, line number)
   */
  def pendingBreakpointsForFile(fileName: String): Seq[BreakpointArgs] = {
    pendingActionManager.getPendingActionData(_.data.fileName == fileName)
      .map(bi => (bi.fileName, bi.lineNumber))
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
    val result = super.removeBreakpointRequest(fileName, lineNumber)

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
    val result = super.removeBreakpointRequestWithId(requestId)

    val pendingResult = pendingActionManager.removePendingActionsWithId(
      requestId
    )

    // True if we removed a real breakpoint or any pending breakpoints
    result || pendingResult.getOrElse(Nil).nonEmpty
  }

  /**
   * Creates and enables a breakpoint on the specified line of the class. Will
   * also remove any pending breakpoints.
   *
   * @param requestId The id of the request used for lookup and removal
   * @param fileName The name of the file to set a breakpoint
   * @param lineNumber The number of the line to break
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  override def createBreakpointRequestWithId(
    requestId: String,
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIRequestArgument*
  ): Try[BreakpointKey] = {
    def createBreakpoint() = super.createBreakpointRequestWithId(
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
          BreakpointInfo(fileName, lineNumber, extraArguments),
          () => createBreakpoint().get
        )
        result
      case _: Throwable => result
    }
  }
}
