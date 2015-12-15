package org.senkbeil.debugger.api.profiles.pure.monitors

import com.sun.jdi.event.{Event, EventQueue}
import com.sun.jdi.request.EventRequestManager
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.lowlevel.events.EventManager
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.senkbeil.debugger.api.lowlevel.monitors.{MonitorContendedEnterManager, MonitorContendedEnterRequestInfo, StandardMonitorContendedEnterManager}
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.senkbeil.debugger.api.lowlevel.events.EventType.MonitorContendedEnterEventType
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.utils.LoopingTaskRunner
import test.JDIMockHelpers

import scala.util.{Failure, Success}

class PureMonitorContendedEnterProfileSpec extends FunSpec with Matchers
with ParallelTestExecution with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockMonitorContendedEnterManager = mock[MonitorContendedEnterManager]
  private val mockEventManager = mock[EventManager]

  private val pureMonitorContendedEnterProfile = new Object with PureMonitorContendedEnterProfile {
    private var requestId: String = _
    def setRequestId(requestId: String): Unit = this.requestId = requestId

    // NOTE: If we set a specific request id, return that, otherwise use the
    //       default behavior
    override protected def newMonitorContendedEnterRequestId(): String =
      if (requestId != null) requestId else super.newMonitorContendedEnterRequestId()

    override protected val monitorContendedEnterManager = mockMonitorContendedEnterManager
    override protected val eventManager: EventManager = mockEventManager
  }

  describe("PureMonitorContendedEnterProfile") {
    describe("#onMonitorContendedEnterWithData") {
      it("should create a new request if one has not be made yet") {
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
        val uniqueIdPropertyFilter = UniqueIdPropertyFilter(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureMonitorContendedEnterProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMonitorContendedEnterManager.createMonitorContendedEnterRequestWithId _)
            .expects(TestRequestId, uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId)).once()

          (mockEventManager.addEventDataStream _)
            .expects(MonitorContendedEnterEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMonitorContendedEnterProfile.onMonitorContendedEnterWithData(
          arguments: _*
        )
      }

      it("should capture exceptions thrown when creating the request") {
        val expected = Failure(new Throwable)
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureMonitorContendedEnterProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMonitorContendedEnterManager.createMonitorContendedEnterRequestWithId _)
            .expects(TestRequestId, uniqueIdProperty +: arguments)
            .throwing(expected.failed.get).once()
        }

        val actual = pureMonitorContendedEnterProfile.onMonitorContendedEnterWithData(
          arguments: _*
        )

        actual should be (expected)
      }

      it("should create a new request if the previous one was removed") {
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureMonitorContendedEnterProfile.setRequestId(TestRequestId)

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMonitorContendedEnterProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMonitorContendedEnterManager.createMonitorContendedEnterRequestWithId _)
            .expects(TestRequestId, uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId)).once()

          (mockEventManager.addEventDataStream _)
            .expects(MonitorContendedEnterEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMonitorContendedEnterProfile.onMonitorContendedEnterWithData(
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMonitorContendedEnterProfile.setRequestId(TestRequestId + "other")

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId + "other")
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId + "other")

          // Return empty this time to indicate that the vm death request
          // was removed some time between the two calls
          (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMonitorContendedEnterManager.createMonitorContendedEnterRequestWithId _)
            .expects(TestRequestId + "other", uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId + "other")).once()

          (mockEventManager.addEventDataStream _)
            .expects(MonitorContendedEnterEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMonitorContendedEnterProfile.onMonitorContendedEnterWithData(
          arguments: _*
        )
      }

      it("should not create a new request if the previous one still exists") {
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMonitorContendedEnterProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockMonitorContendedEnterManager.createMonitorContendedEnterRequestWithId _)
            .expects(TestRequestId, uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId)).once()

          (mockEventManager.addEventDataStream _)
            .expects(MonitorContendedEnterEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMonitorContendedEnterProfile.onMonitorContendedEnterWithData(
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureMonitorContendedEnterProfile.setRequestId(TestRequestId + "other")

          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Return collection of matching arguments to indicate that we do
          // still have the request
          val internalId = java.util.UUID.randomUUID().toString
          (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _)
            .expects()
            .returning(Seq(internalId)).once()
          (mockMonitorContendedEnterManager.getMonitorContendedEnterRequestInfo _)
            .expects(internalId)
            .returning(Some(MonitorContendedEnterRequestInfo(TestRequestId, arguments))).once()

          (mockEventManager.addEventDataStream _)
            .expects(MonitorContendedEnterEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureMonitorContendedEnterProfile.onMonitorContendedEnterWithData(
          arguments: _*
        )
      }

      it("should remove the underlying request if all pipelines are closed") {
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureMonitorContendedEnterProfile.setRequestId(TestRequestId)

        inSequence {
          inAnyOrder {
            val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
            val uniqueIdPropertyFilter =
              UniqueIdPropertyFilter(id = TestRequestId)

            // Memoized request function first checks to make sure the cache
            // has not been invalidated underneath (first call will always be
            // empty since we have never created the request)
            (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _)
              .expects()
              .returning(Nil).once()
            (mockMonitorContendedEnterManager.monitorContendedEnterRequestList _)
              .expects()
              .returning(Seq(TestRequestId)).once()

            (mockMonitorContendedEnterManager.getMonitorContendedEnterRequestInfo _)
              .expects(TestRequestId)
              .returning(Some(MonitorContendedEnterRequestInfo(TestRequestId, arguments))).once()

            // NOTE: Expect the request to be created with a unique id
            (mockMonitorContendedEnterManager.createMonitorContendedEnterRequestWithId _)
              .expects(TestRequestId, uniqueIdProperty +: arguments)
              .returning(Success(TestRequestId)).once()

            (mockEventManager.addEventDataStream _)
              .expects(MonitorContendedEnterEventType, Seq(uniqueIdPropertyFilter))
              .returning(Pipeline.newPipeline(
                classOf[(Event, Seq[JDIEventDataResult])]
              )).twice()
          }

          (mockMonitorContendedEnterManager.removeMonitorContendedEnterRequest _)
            .expects(TestRequestId).once()
        }

        val p1 = pureMonitorContendedEnterProfile.onMonitorContendedEnterWithData(arguments: _*)
        val p2 = pureMonitorContendedEnterProfile.onMonitorContendedEnterWithData(arguments: _*)

        p1.foreach(_.close())
        p2.foreach(_.close())
      }
    }
  }
}

