package org.senkbeil.debugger.api.profiles.pure.methods

import com.sun.jdi.event.MethodExitEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.senkbeil.debugger.api.lowlevel.events.filters.{UniqueIdPropertyFilter, MethodNameFilter}
import org.senkbeil.debugger.api.lowlevel.methods.MethodExitManager
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.senkbeil.debugger.api.lowlevel.utils.JDIArgumentGroup
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.traits.methods.MethodExitProfile
import org.senkbeil.debugger.api.utils.Memoization
import org.senkbeil.debugger.api.lowlevel.events.EventType.MethodExitEventType

import scala.util.Try

/**
 * Represents a pure profile for method exit that adds no extra logic on top
 * of the standard JDI.
 */
trait PureMethodExitProfile extends MethodExitProfile {
  protected val methodExitManager: MethodExitManager
  protected val eventManager: EventManager

  /**
   * Constructs a stream of method exit events for the specified class and
   * method.
   *
   * @param className The full name of the class/object/trait containing the
   *                  method to watch
   * @param methodName The name of the method to watch
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of method exit events and any retrieved data based on
   *         requests from extra arguments
   */
  override def onMethodExitWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MethodExitEventAndData]] = Try {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)
    val requestId = newMethodExitRequest((className, methodName, rArgs))
    newMethodExitPipeline(requestId, MethodNameFilter(methodName) +: eArgs)
  }

  /**
   * Creates a new method exit request using the given arguments. The request
   * is memoized, meaning that the same request will be returned for the same
   * arguments. The memoized result will be thrown out if the underlying
   * request storage indicates that the request has been removed.
   *
   * @return The id of the created method exit request
   */
  protected val newMethodExitRequest = {
    type Input = (String, String, Seq[JDIRequestArgument])
    type Key = (String, String, Seq[JDIRequestArgument])
    type Output = String

    new Memoization[Input, Key, Output](
      memoFunc = (input: Input) => {
        val requestId = newMethodExitRequestId()
        val args = UniqueIdProperty(id = requestId) +: input._3

        methodExitManager.createMethodExitRequest(
          input._1,
          input._2,
          args: _*
        ).get

        requestId
      },
      cacheInvalidFunc = (key: Key) => {
        !methodExitManager.hasMethodExitRequest(key._1, key._2)
      }
    )
  }

  /**
   * Creates a new pipeline of method exit events and data using the given
   * arguments. The pipeline is NOT memoized; therefore, each call creates a
   * new pipeline with a new underlying event handler feeding the pipeline.
   * This means that the pipeline needs to be properly closed to remove the
   * event handler.
   *
   * @param requestId The id of the request whose events to stream through the
   *                  new pipeline
   * @param args The additional event arguments to provide to the event handler
   *             feeding the new pipeline
   * @return The new method exit event and data pipeline
   */
  protected def newMethodExitPipeline(
    requestId: String,
    args: Seq[JDIEventArgument]
  ): IdentityPipeline[MethodExitEventAndData] = {
    val eArgsWithFilter = UniqueIdPropertyFilter(id = requestId) +: args
    eventManager.addEventDataStream(MethodExitEventType, eArgsWithFilter: _*)
      .map(t => (t._1.asInstanceOf[MethodExitEvent], t._2)).noop()
  }

  /**
   * Used to generate new request ids to capture request/event matches.
   *
   * @return The new id as a string
   */
  protected def newMethodExitRequestId(): String =
    java.util.UUID.randomUUID().toString
}