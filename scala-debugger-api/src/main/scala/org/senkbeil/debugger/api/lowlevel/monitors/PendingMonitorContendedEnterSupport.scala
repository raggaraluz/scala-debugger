package org.senkbeil.debugger.api.lowlevel.monitors

import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.utils.PendingActionManager

import scala.util.{Failure, Success, Try}

/**
 * Provides pending monitor contended enter capabilities to an existing
 * monitor contended enter manager.
 */
trait PendingMonitorContendedEnterSupport extends MonitorContendedEnterManager {
  /**
   * Represents the manager used to store pending monitor contended enter requests and
   * process them later.
   */
  protected val pendingActionManager: PendingActionManager[MonitorContendedEnterRequestInfo]

  /** When enabled, results in adding any failed request as pending. */
  @volatile var enablePending: Boolean = true

  /**
   * Processes all pending monitor contended enter requests.
   *
   * @return The collection of successfully-processed monitor contended enter requests
   */
  def processAllPendingMonitorContendedEnterRequests(): Seq[MonitorContendedEnterRequestInfo] = {
    pendingActionManager.processAllActions().map(_.data)
  }

  /**
   * Retrieves a list of pending monitor contended enter requests.
   *
   * @return The collection of monitor contended enter requests
   */
  def pendingMonitorContendedEnterRequests: Seq[MonitorContendedEnterRequestInfo] = {
    pendingActionManager.getPendingActionData(_ => true)
  }

  /**
   * Creates a new monitor contended enter request.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  abstract override def createMonitorContendedEnterRequest(
    extraArguments: JDIRequestArgument*
  ): Try[String] = createMonitorContendedEnterRequestWithId(
    newRequestId(),
    extraArguments: _*
  )

  /**
   * Creates a new monitor contended enter request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  abstract override def createMonitorContendedEnterRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    def createMonitorContendedEnterRequest() = super.createMonitorContendedEnterRequestWithId(
      requestId,
      extraArguments: _*
    )

    val result = createMonitorContendedEnterRequest()

    // If failed, add as pending
    result.recoverWith {
      case _: Throwable if enablePending =>
        pendingActionManager.addPendingActionWithId(
          requestId,
          MonitorContendedEnterRequestInfo(extraArguments),
          () => createMonitorContendedEnterRequest().get
        )
        Success(requestId)
      case throwable: Throwable if !enablePending =>
        Failure(throwable)
    }
  }

  /**
   * Removes the specified monitor contended enter request.
   *
   * @param id The id of the Class Unload Request
   *
   * @return True if the monitor contended enter request was removed (if it existed),
   *         otherwise false
   */
  abstract override def removeMonitorContendedEnterRequest(id: String): Boolean = {
    val result = super.removeMonitorContendedEnterRequest(id)

    val pendingResult = pendingActionManager.removePendingActionsWithId(id)

    // True if we removed a real monitor contended enter request or any
    // pending monitor contended enter request
    result || pendingResult.getOrElse(Nil).nonEmpty
  }
}

