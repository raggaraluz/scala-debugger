package org.senkbeil.debugger.api.profiles.pure.methods

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.event.MethodEntryEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.senkbeil.debugger.api.lowlevel.events.filters.{UniqueIdPropertyFilter, MethodNameFilter}
import org.senkbeil.debugger.api.lowlevel.methods.MethodEntryManager
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.senkbeil.debugger.api.lowlevel.utils.JDIArgumentGroup
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.traits.methods.MethodEntryProfile
import org.senkbeil.debugger.api.utils.Memoization
import org.senkbeil.debugger.api.lowlevel.events.EventType.MethodEntryEventType

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents a pure profile for method entry that adds no extra logic on top
 * of the standard JDI.
 */
trait PureMethodEntryProfile extends MethodEntryProfile {
  protected val methodEntryManager: MethodEntryManager
  protected val eventManager: EventManager

  /**
   * Contains mapping from input to a counter indicating how many pipelines
   * are currently active for the input.
   */
  private val pipelineCounter = new ConcurrentHashMap[
    (String, String, Seq[JDIArgument]),
    AtomicInteger
    ]().asScala

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
  ): Try[IdentityPipeline[MethodEntryEventAndData]] = Try {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)
    val requestId = newMethodEntryRequest((className, methodName, rArgs))
    newMethodEntryPipeline(
      requestId,
      (className, methodName, MethodNameFilter(methodName) +: eArgs)
    )
  }

  /**
   * Creates a new method entry request using the given arguments. The request
   * is memoized, meaning that the same request will be returned for the same
   * arguments. The memoized result will be thrown out if the underlying
   * request storage indicates that the request has been removed.
   *
   * @return The id of the created method entry request
   */
  protected val newMethodEntryRequest = {
    type Input = (String, String, Seq[JDIRequestArgument])
    type Key = (String, String, Seq[JDIRequestArgument])
    type Output = String

    new Memoization[Input, Key, Output](
      memoFunc = (input: Input) => {
        val requestId = newMethodEntryRequestId()
        val args = UniqueIdProperty(id = requestId) +: input._3

        methodEntryManager.createMethodEntryRequestWithId(
          requestId,
          input._1,
          input._2,
          args: _*
        ).get

        requestId
      },
      cacheInvalidFunc = (key: Key) => {
        !methodEntryManager.hasMethodEntryRequest(key._1, key._2)
      }
    )
  }

  /**
   * Creates a new pipeline of method entry events and data using the given
   * arguments. The pipeline is NOT memoized; therefore, each call creates a
   * new pipeline with a new underlying event handler feeding the pipeline.
   * This means that the pipeline needs to be properly closed to remove the
   * event handler.
   *
   * @param requestId The id of the request whose events to stream through the
   *                  new pipeline
   * @param args The additional event arguments to provide to the event handler
   *             feeding the new pipeline
   * @return The new method entry event and data pipeline
   */
  protected def newMethodEntryPipeline(
    requestId: String,
    args: (String, String, Seq[JDIEventArgument])
  ): IdentityPipeline[MethodEntryEventAndData] = {
    val eArgsWithFilter = UniqueIdPropertyFilter(id = requestId) +: args._3
    val newPipeline = eventManager
      .addEventDataStream(MethodEntryEventType, eArgsWithFilter: _*)
      .map(t => (t._1.asInstanceOf[MethodEntryEvent], t._2))
      .noop()

    // Create a companion pipeline who, when closed, checks to see if there
    // are no more pipelines for the given request and, if so, removes the
    // request as well
    val closePipeline = Pipeline.newPipeline(
      classOf[MethodEntryEventAndData],
      newMethodEntryPipelineCloseFunc(requestId, args)
    )

    // Increment the counter for open pipelines
    pipelineCounter
      .getOrElseUpdate(args, new AtomicInteger(0))
      .incrementAndGet()

    val combinedPipeline = newPipeline.unionOutput(closePipeline)
    combinedPipeline
  }

  /**
   * Creates a new function used for closing generated pipelines.
   *
   * @param requestId The id of the request
   * @param args The arguments associated with the request
   *
   * @return The new function for closing the pipeline
   */
  protected def newMethodEntryPipelineCloseFunc(
    requestId: String,
    args: (String, String, Seq[JDIEventArgument])
  ): () => Unit = () => {
    val pCounter = pipelineCounter(args)

    val totalPipelinesRemaining = pCounter.decrementAndGet()

    if (totalPipelinesRemaining == 0) {
      methodEntryManager.removeMethodEntryRequestWithId(requestId)
    }
  }

  /**
   * Used to generate new request ids to capture request/event matches.
   *
   * @return The new id as a string
   */
  protected def newMethodEntryRequestId(): String =
    java.util.UUID.randomUUID().toString
}

