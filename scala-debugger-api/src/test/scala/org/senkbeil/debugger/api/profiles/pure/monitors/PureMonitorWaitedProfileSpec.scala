package org.senkbeil.debugger.api.profiles.pure.monitors

import com.sun.jdi.event.{Event, EventQueue}
import com.sun.jdi.request.EventRequestManager
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.events.EventManager
import org.senkbeil.debugger.api.lowlevel.events.EventType.MonitorWaitedEventType
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.senkbeil.debugger.api.lowlevel.monitors.MonitorWaitedManager
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.utils.LoopingTaskRunner
import test.JDIMockHelpers

import scala.util.{Failure, Success}

class PureMonitorWaitedProfileSpec extends FunSpec with Matchers
with OneInstancePerTest with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString

  // Workaround - see https://github.com/paulbutcher/ScalaMock/issues/33
  private class ZeroArgMonitorWaitedManager extends MonitorWaitedManager(
    stub[EventRequestManager]
  )
  private val mockMonitorWaitedManager = mock[ZeroArgMonitorWaitedManager]

  // Workaround - see https://github.com/paulbutcher/ScalaMock/issues/33
  private class ZeroArgEventManager extends EventManager(
    stub[EventQueue],
    stub[LoopingTaskRunner],
    autoStart = false
  )
  private val mockEventManager = mock[ZeroArgEventManager]

  private val pureMonitorWaitedProfile = new Object with PureMonitorWaitedProfile {
    private var requestId: String = _
    def setRequestId(requestId: String): Unit = this.requestId = requestId

    // NOTE: If we set a specific request id, return that, otherwise use the
    //       default behavior
    override protected def newMonitorWaitedRequestId(): String =
      if (requestId != null) requestId else super.newMonitorWaitedRequestId()

    override protected val monitorWaitedManager = mockMonitorWaitedManager
    override protected val eventManager: EventManager = mockEventManager
  }

  describe("PureMonitorWaitedProfile") {
    describe("#onMonitorWaitedWithData") {
      it("should create a new request if one has not be made yet") {
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
        val uniqueIdPropertyFilter = UniqueIdPropertyFilter(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureMonitorWaitedProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockMonitorWaitedManager.monitorWaitedRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMonitorWaitedManager.createMonitorWaitedRequest _)
            .expects(uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId)).once()

          (mockEventManager.addEventDataStream _)
            .expects(MonitorWaitedEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMonitorWaitedProfile.onMonitorWaitedWithData(
          arguments: _*
        )
      }

      it("should capture exceptions thrown when creating the request") {
        val expected = Failure(new Throwable)
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureMonitorWaitedProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockMonitorWaitedManager.monitorWaitedRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMonitorWaitedManager.createMonitorWaitedRequest _)
            .expects(uniqueIdProperty +: arguments)
            .throwing(expected.failed.get).once()
        }

        val actual = pureMonitorWaitedProfile.onMonitorWaitedWithData(
          arguments: _*
        )

        actual should be (expected)
      }

      it("should create a new request if the previous one was removed") {
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureMonitorWaitedProfile.setRequestId(TestRequestId)

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMonitorWaitedProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockMonitorWaitedManager.monitorWaitedRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMonitorWaitedManager.createMonitorWaitedRequest _)
            .expects(uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId)).once()

          (mockEventManager.addEventDataStream _)
            .expects(MonitorWaitedEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMonitorWaitedProfile.onMonitorWaitedWithData(
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMonitorWaitedProfile.setRequestId(TestRequestId + "other")

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId + "other")
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId + "other")

          // Return empty this time to indicate that the class prepare request
          // was removed some time between the two calls
          (mockMonitorWaitedManager.monitorWaitedRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMonitorWaitedManager.createMonitorWaitedRequest _)
            .expects(uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId + "other")).once()

          (mockEventManager.addEventDataStream _)
            .expects(MonitorWaitedEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMonitorWaitedProfile.onMonitorWaitedWithData(
          arguments: _*
        )
      }

      it("should not create a new request if the previous one still exists") {
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMonitorWaitedProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockMonitorWaitedManager.monitorWaitedRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMonitorWaitedManager.createMonitorWaitedRequest _)
            .expects(uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId)).once()

          (mockEventManager.addEventDataStream _)
            .expects(MonitorWaitedEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMonitorWaitedProfile.onMonitorWaitedWithData(
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMonitorWaitedProfile.setRequestId(TestRequestId + "other")

          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Return collection of matching arguments to indicate that we do
          // still have the request
          val internalId = java.util.UUID.randomUUID().toString
          (mockMonitorWaitedManager.monitorWaitedRequestList _)
            .expects()
            .returning(Seq(internalId)).once()
          (mockMonitorWaitedManager.getMonitorWaitedRequestArguments _)
            .expects(internalId)
            .returning(Some(arguments)).once()

          (mockEventManager.addEventDataStream _)
            .expects(MonitorWaitedEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMonitorWaitedProfile.onMonitorWaitedWithData(
          arguments: _*
        )
      }
    }
  }
}

