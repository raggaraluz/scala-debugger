package test

import com.sun.jdi.request.ThreadDeathRequest
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.threads.{ThreadDeathRequestInfo, ThreadDeathManager}

import scala.util.Try

/**
 * Test thread death manager that merely invokes the provided
 * thread death manager underneath to make it easier to mock.
 *
 * @param threadDeathManager The underlying thread death manager used to
 *                            execute all methods
 */
class TestThreadDeathManager(
  private val threadDeathManager: ThreadDeathManager
) extends ThreadDeathManager {
  override def threadDeathRequestList: Seq[String] =
    threadDeathManager.threadDeathRequestList
  override def createThreadDeathRequest(extraArguments: JDIRequestArgument*): Try[String] =
    threadDeathManager.createThreadDeathRequest(extraArguments: _*)
  override def hasThreadDeathRequest(id: String): Boolean =
    threadDeathManager.hasThreadDeathRequest(id)
  override def getThreadDeathRequest(id: String): Option[ThreadDeathRequest] =
    threadDeathManager.getThreadDeathRequest(id)
  override def createThreadDeathRequestWithId(requestId: String, extraArguments: JDIRequestArgument*): Try[String] =
    threadDeathManager.createThreadDeathRequestWithId(requestId, extraArguments: _*)
  override def removeThreadDeathRequest(id: String): Boolean =
    threadDeathManager.removeThreadDeathRequest(id)
  override def getThreadDeathRequestInfo(id: String): Option[ThreadDeathRequestInfo] =
    threadDeathManager.getThreadDeathRequestInfo(id)
}
