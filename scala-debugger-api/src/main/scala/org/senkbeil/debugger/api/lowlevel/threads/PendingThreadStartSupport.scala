package org.senkbeil.debugger.api.lowlevel.threads

import org.senkbeil.debugger.api.lowlevel.PendingRequestSupport
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.utils.PendingActionManager

import scala.util.{Failure, Success, Try}

/**
 * Provides pending thread start capabilities to an existing
 * thread start manager.
 */
trait PendingThreadStartSupport
  extends ThreadStartManager
  with PendingRequestSupport
{
  /**
   * Represents the manager used to store pending thread start requests and
   * process them later.
   */
  protected val pendingActionManager: PendingActionManager[ThreadStartRequestInfo]

  /**
   * Processes all pending thread start requests.
   *
   * @return The collection of successfully-processed thread start requests
   */
  def processAllPendingThreadStartRequests(): Seq[ThreadStartRequestInfo] = {
    pendingActionManager.processAllActions().map(_.data)
  }

  /**
   * Retrieves a list of pending thread start requests.
   *
   * @return The collection of thread start requests
   */
  def pendingThreadStartRequests: Seq[ThreadStartRequestInfo] = {
    pendingActionManager.getPendingActionData(_ => true)
  }

  /**
   * Creates a new thread start request.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  abstract override def createThreadStartRequest(
    extraArguments: JDIRequestArgument*
  ): Try[String] = createThreadStartRequestWithId(
    newRequestId(),
    extraArguments: _*
  )

  /**
   * Creates a new thread start request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  abstract override def createThreadStartRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    def createThreadStartRequest() = super.createThreadStartRequestWithId(
      requestId,
      extraArguments: _*
    )

    val result = createThreadStartRequest()

    // If failed, add as pending
    result.recoverWith {
      case _: Throwable if isPendingSupportEnabled =>
        pendingActionManager.addPendingActionWithId(
          requestId,
          ThreadStartRequestInfo(extraArguments),
          () => createThreadStartRequest().get
        )
        Success(requestId)
      case throwable: Throwable if !isPendingSupportEnabled =>
        Failure(throwable)
    }
  }

  /**
   * Removes the specified thread start request.
   *
   * @param id The id of the Class Unload Request
   *
   * @return True if the thread start request was removed (if it existed),
   *         otherwise false
   */
  abstract override def removeThreadStartRequest(id: String): Boolean = {
    val result = super.removeThreadStartRequest(id)

    val pendingResult = pendingActionManager.removePendingActionsWithId(id)

    // True if we removed a real thread start request or any
    // pending thread start request
    result || pendingResult.getOrElse(Nil).nonEmpty
  }
}

