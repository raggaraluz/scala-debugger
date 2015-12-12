package org.senkbeil.debugger.api.lowlevel.vm

import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.utils.PendingActionManager

import scala.util.{Failure, Success, Try}

/**
 * Provides pending vm death capabilities to an existing
 * vm death manager.
 */
trait PendingVMDeathSupport extends VMDeathManager {
  /**
   * Represents the manager used to store pending vm death requests and
   * process them later.
   */
  protected val pendingActionManager: PendingActionManager[VMDeathRequestInfo]

  /** When enabled, results in adding any failed request as pending. */
  @volatile var enablePending: Boolean = true

  /**
   * Processes all pending vm death requests.
   *
   * @return The collection of successfully-processed vm death requests
   */
  def processAllPendingVMDeathRequests(): Seq[VMDeathRequestInfo] = {
    pendingActionManager.processAllActions().map(_.data)
  }

  /**
   * Retrieves a list of pending vm death requests.
   *
   * @return The collection of vm death requests
   */
  def pendingVMDeathRequests: Seq[VMDeathRequestInfo] = {
    pendingActionManager.getPendingActionData(_ => true)
  }

  /**
   * Creates a new vm death request.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  abstract override def createVMDeathRequest(
    extraArguments: JDIRequestArgument*
  ): Try[String] = createVMDeathRequestWithId(
    newRequestId(),
    extraArguments: _*
  )

  /**
   * Creates a new vm death request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  abstract override def createVMDeathRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    def createVMDeathRequest() = super.createVMDeathRequestWithId(
      requestId,
      extraArguments: _*
    )

    val result = createVMDeathRequest()

    // If failed, add as pending
    result.recoverWith {
      case _: Throwable if enablePending =>
        pendingActionManager.addPendingActionWithId(
          requestId,
          VMDeathRequestInfo(extraArguments),
          () => createVMDeathRequest().get
        )
        Success(requestId)
      case throwable: Throwable if !enablePending =>
        Failure(throwable)
    }
  }

  /**
   * Removes the specified vm death request.
   *
   * @param id The id of the Class Unload Request
   *
   * @return True if the vm death request was removed (if it existed),
   *         otherwise false
   */
  abstract override def removeVMDeathRequest(id: String): Boolean = {
    val result = super.removeVMDeathRequest(id)

    val pendingResult = pendingActionManager.removePendingActionsWithId(id)

    // True if we removed a real vm death request or any
    // pending vm death request
    result || pendingResult.getOrElse(Nil).nonEmpty
  }
}

