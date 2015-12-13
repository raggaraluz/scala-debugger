package test

import com.sun.jdi.ThreadReference
import com.sun.jdi.request.StepRequest
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.steps.{StepRequestInfo, StepManager}

import scala.util.Try

class TestStepManager(
  private val stepManager: StepManager
) extends StepManager {
  override def createStepRequest(threadReference: ThreadReference, size: Int, depth: Int, extraArguments: JDIRequestArgument*): Try[String] =
    stepManager.createStepRequest(threadReference, size, depth, extraArguments: _*)
  override def getStepRequestInfoWithId(requestId: String): Option[StepRequestInfo] =
    stepManager.getStepRequestInfoWithId(requestId)
  override def hasStepRequestWithId(requestId: String): Boolean =
    stepManager.hasStepRequestWithId(requestId)
  override def hasStepRequest(threadReference: ThreadReference): Boolean =
    stepManager.hasStepRequest(threadReference)
  override def stepRequestListById: Seq[String] =
    stepManager.stepRequestListById
  override def getStepRequest(threadReference: ThreadReference): Option[Seq[StepRequest]] =
    stepManager.getStepRequest(threadReference)
  override def createStepRequestWithId(requestId: String, threadReference: ThreadReference, size: Int, depth: Int, extraArguments: JDIRequestArgument*): Try[String] =
    stepManager.createStepRequestWithId(requestId, threadReference, size, depth, extraArguments: _*)
  override def createStepRequestWithId(requestId: String, removeExistingRequests: Boolean, threadReference: ThreadReference, size: Int, depth: Int, extraArguments: JDIRequestArgument*): Try[String] =
    stepManager.createStepRequestWithId(requestId, removeExistingRequests, threadReference, size, depth, extraArguments: _*)
  override def getStepRequestWithId(requestId: String): Option[StepRequest] =
    stepManager.getStepRequestWithId(requestId)
  override def removeStepRequest(threadReference: ThreadReference): Boolean =
    stepManager.removeStepRequest(threadReference)
  override def stepRequestList: Seq[StepRequestInfo] =
    stepManager.stepRequestList
  override def removeStepRequestWithId(requestId: String): Boolean =
    stepManager.removeStepRequestWithId(requestId)
}
