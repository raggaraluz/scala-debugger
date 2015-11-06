package org.senkbeil.debugger.api.profiles.pure.threads

import com.sun.jdi.event.ThreadDeathEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.threads.ThreadDeathManager
import org.senkbeil.debugger.api.lowlevel.utils.JDIRequestResponseBuilder
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.traits.threads.ThreadDeathProfile

import scala.util.Try

/**
 * Represents a pure profile for thread death events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureThreadDeathProfile extends ThreadDeathProfile {
  protected val threadDeathManager: ThreadDeathManager
  protected val requestResponseBuilder: JDIRequestResponseBuilder

  /**
   * Constructs a stream of thread death events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of thread death events and any retrieved data based on
   *         requests from extra arguments
   */
  override def onThreadDeathWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ThreadDeathEventAndData]] = {
    /** Creates a new request using arguments. */
    def newRequest(args: Seq[JDIRequestArgument]): Unit = {
      // Ignore the id and propagate up any errors
      threadDeathManager.createThreadDeathRequest(args: _*).get
    }

    requestResponseBuilder.buildRequestResponse[ThreadDeathEvent](
      newRequest,
      extraArguments: _*
    )
  }
}
