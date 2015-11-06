package org.senkbeil.debugger.api.profiles.pure.threads

import com.sun.jdi.event.ThreadStartEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.threads.ThreadStartManager
import org.senkbeil.debugger.api.lowlevel.utils.JDIRequestResponseBuilder
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.traits.threads.ThreadStartProfile

import scala.util.Try

/**
 * Represents a pure profile for thread start events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureThreadStartProfile extends ThreadStartProfile {
  protected val threadStartManager: ThreadStartManager
  protected val requestResponseBuilder: JDIRequestResponseBuilder

  /**
   * Constructs a stream of thread start events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of thread start events and any retrieved data based on
   *         requests from extra arguments
   */
  override def onThreadStartWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ThreadStartEventAndData]] = {
    /** Creates a new request using arguments. */
    def newRequest(args: Seq[JDIRequestArgument]): Unit = {
      // Ignore the id and propagate up any errors
      threadStartManager.createThreadStartRequest(args: _*).get
    }

    requestResponseBuilder.buildRequestResponse[ThreadStartEvent](
      newRequest,
      extraArguments: _*
    )
  }
}
