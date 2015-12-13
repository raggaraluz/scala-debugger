package org.senkbeil.debugger.api.lowlevel.monitors

import org.senkbeil.debugger.api.lowlevel.PendingRequestSupport
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.utils.PendingActionManager

import scala.util.{Failure, Success, Try}

/**
 * Provides pending monitor waited capabilities to an existing
 * monitor waited manager.
 */
trait PendingMonitorWaitedSupport
  extends MonitorWaitedManager
  with PendingRequestSupport
{
  /**
   * Represents the manager used to store pending monitor waited requests and
   * process them later.
   */
  protected val pendingActionManager: PendingActionManager[MonitorWaitedRequestInfo]

  /**
   * Processes all pending monitor waited requests.
   *
   * @return The collection of successfully-processed monitor waited requests
   */
  def processAllPendingMonitorWaitedRequests(): Seq[MonitorWaitedRequestInfo] = {
    pendingActionManager.processAllActions().map(_.data)
  }

  /**
   * Retrieves a list of pending monitor waited requests.
   *
   * @return The collection of monitor waited requests
   */
  def pendingMonitorWaitedRequests: Seq[MonitorWaitedRequestInfo] = {
    pendingActionManager.getPendingActionData(_ => true)
  }

  /**
   * Creates a new monitor waited request.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  abstract override def createMonitorWaitedRequest(
    extraArguments: JDIRequestArgument*
  ): Try[String] = createMonitorWaitedRequestWithId(
    newRequestId(),
    extraArguments: _*
  )

  /**
   * Creates a new monitor waited request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  abstract override def createMonitorWaitedRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    def createMonitorWaitedRequest() = super.createMonitorWaitedRequestWithId(
      requestId,
      extraArguments: _*
    )

    val result = createMonitorWaitedRequest()

    // If failed, add as pending
    result.recoverWith {
      case _: Throwable if isPendingSupportEnabled =>
        pendingActionManager.addPendingActionWithId(
          requestId,
          MonitorWaitedRequestInfo(requestId, extraArguments),
          () => createMonitorWaitedRequest().get
        )
        Success(requestId)
      case _: Throwable => result
    }
  }

  /**
   * Removes the specified monitor waited request.
   *
   * @param id The id of the Class Unload Request
   *
   * @return True if the monitor waited request was removed (if it existed),
   *         otherwise false
   */
  abstract override def removeMonitorWaitedRequest(id: String): Boolean = {
    val result = super.removeMonitorWaitedRequest(id)

    val pendingResult = pendingActionManager.removePendingActionsWithId(id)

    // True if we removed a real monitor waited request or any
    // pending monitor waited request
    result || pendingResult.getOrElse(Nil).nonEmpty
  }
}

