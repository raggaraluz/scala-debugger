package org.senkbeil.debugger.api.lowlevel.utils

import com.sun.jdi.event.Event
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.senkbeil.debugger.api.lowlevel.events.{EventManager, EventType}
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline

import scala.reflect.ClassTag
import scala.util.Try

/**
 * Represents a builder for JDI request --> response model where each build
 * invokes a function to generate a request that will map to a stream of
 * responses.
 *
 * @param eventManager The low-level event manager to use to bind to incoming
 *                     events resulting from the request
 */
class JDIRequestResponseBuilder(private val eventManager: EventManager) {
  /**
   * Constructs a stream of events based on the provided respon.
   *
   * @tparam A The type of event used to build the request and serve as the
   *           event stream in the response
   * @param requestBuilderFunc The function used to generate a JDI request
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of breakpoint events and any retrieved data based on
   *         requests from extra arguments
   */
  def buildRequestResponse[A <: Event](
    requestBuilderFunc: (Seq[JDIRequestArgument]) => Unit,
    extraArguments: JDIArgument*
  )(
    implicit classTag: ClassTag[A]
  ): Try[IdentityPipeline[(A, Seq[JDIEventDataResult])]] = Try {
    val requestId = newRequestId()

    // TODO: Determine why a casting is needed here
    //       (Class[_$1] found instead of Class[A])
    val eventType = {
      val eventClass = classTag.runtimeClass
      val _eventType = EventType.eventClassToEventType(
        eventClass.asInstanceOf[Class[A]]
      )

      assert(_eventType.nonEmpty, s"No matching type for event: $eventClass")

      _eventType.get
    }

    val idProperty = UniqueIdProperty(id = requestId)
    val idFilter = UniqueIdPropertyFilter(id = requestId)

    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(
      idProperty +: extraArguments :+ idFilter: _*
    )

    requestBuilderFunc(rArgs)

    transformStream[A](eventManager.addEventDataStream(eventType, eArgs: _*))
  }

  /**
   * Transforms a stream of standard (event, data) to (specific event, data).
   *
   * @param pipeline The pipeline to transform
   * @tparam A The new event type of the transformed pipeline
   *
   * @return The transformed pipeline
   */
  protected def transformStream[A <: Event : ClassTag](
    pipeline: Pipeline[(Event, Seq[JDIEventDataResult]), (Event, Seq[JDIEventDataResult])]
  ): Pipeline[(A, Seq[JDIEventDataResult]), (A, Seq[JDIEventDataResult])] = {
    pipeline map { case (event, data) => (event.asInstanceOf[A], data) } noop()
  }

  /** Used to generate new request ids to capture request/event matches. */
  protected def newRequestId(): String = java.util.UUID.randomUUID().toString
}
