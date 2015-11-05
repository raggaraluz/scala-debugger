package org.senkbeil.debugger.api.profiles.pure.methods

import com.sun.jdi.event.MethodEntryEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.filters.MethodNameFilter
import org.senkbeil.debugger.api.lowlevel.methods.MethodEntryManager
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.utils.JDIRequestResponseBuilder
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.traits.methods.MethodEntryProfile

import scala.util.Try

/**
 * Represents a pure profile for method entry that adds no extra logic on top
 * of the standard JDI.
 */
trait PureMethodEntryProfile extends MethodEntryProfile {
  protected val methodEntryManager: MethodEntryManager
  protected val requestResponseBuilder: JDIRequestResponseBuilder

  /**
   * Constructs a stream of method entry events for the specified class and
   * method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method to watch
   * @param methodName The name of the method to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of method entry events and any retrieved data based on
   *         requests from extra arguments
   */
  override def onMethodEntryWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MethodEntryEventAndData]] = {
    /** Creates a new request using arguments. */
    def newRequest(args: Seq[JDIRequestArgument]): Unit = {
      // Ignore true/false, but propagate up any errors
      methodEntryManager.setMethodEntry(
        className,
        methodName,
        args: _*
      )
    }

    requestResponseBuilder.buildRequestResponse[MethodEntryEvent](
      newRequest,
      MethodNameFilter(name = methodName) +: extraArguments: _*
    )
  }
}
