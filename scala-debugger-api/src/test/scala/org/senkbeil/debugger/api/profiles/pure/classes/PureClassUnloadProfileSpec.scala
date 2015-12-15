package org.senkbeil.debugger.api.profiles.pure.classes

import com.sun.jdi.event.{Event, EventQueue}
import com.sun.jdi.request.EventRequestManager
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.senkbeil.debugger.api.lowlevel.classes.{ClassUnloadManager, ClassUnloadRequestInfo, StandardClassUnloadManager}
import org.senkbeil.debugger.api.lowlevel.events.EventManager
import org.senkbeil.debugger.api.lowlevel.events.EventType.ClassUnloadEventType
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.utils.LoopingTaskRunner
import test.JDIMockHelpers

import scala.util.{Failure, Success}

class PureClassUnloadProfileSpec extends FunSpec with Matchers
with ParallelTestExecution with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockClassUnloadManager = mock[ClassUnloadManager]
  private val mockEventManager = mock[EventManager]

  private val pureClassUnloadProfile = new Object with PureClassUnloadProfile {
    private var requestId: String = _
    def setRequestId(requestId: String): Unit = this.requestId = requestId

    // NOTE: If we set a specific request id, return that, otherwise use the
    //       default behavior
    override protected def newClassUnloadRequestId(): String =
      if (requestId != null) requestId else super.newClassUnloadRequestId()

    override protected val classUnloadManager = mockClassUnloadManager
    override protected val eventManager: EventManager = mockEventManager
  }

  describe("PureClassUnloadProfile") {
    describe("#onClassUnloadWithData") {
      it("should create a new request if one has not be made yet") {
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
        val uniqueIdPropertyFilter = UniqueIdPropertyFilter(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureClassUnloadProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockClassUnloadManager.classUnloadRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockClassUnloadManager.createClassUnloadRequestWithId _)
            .expects(TestRequestId, uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId)).once()

          (mockEventManager.addEventDataStream _)
            .expects(ClassUnloadEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureClassUnloadProfile.onClassUnloadWithData(
          arguments: _*
        )
      }

      it("should capture exceptions thrown when creating the request") {
        val expected = Failure(new Throwable)
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureClassUnloadProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockClassUnloadManager.classUnloadRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockClassUnloadManager.createClassUnloadRequestWithId _)
            .expects(TestRequestId, uniqueIdProperty +: arguments)
            .throwing(expected.failed.get).once()
        }

        val actual = pureClassUnloadProfile.onClassUnloadWithData(
          arguments: _*
        )

        actual should be (expected)
      }

      it("should create a new request if the previous one was removed") {
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureClassUnloadProfile.setRequestId(TestRequestId)

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureClassUnloadProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockClassUnloadManager.classUnloadRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockClassUnloadManager.createClassUnloadRequestWithId _)
            .expects(TestRequestId, uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId)).once()

          (mockEventManager.addEventDataStream _)
            .expects(ClassUnloadEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureClassUnloadProfile.onClassUnloadWithData(
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureClassUnloadProfile.setRequestId(TestRequestId + "other")

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId + "other")
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId + "other")

          // Return empty this time to indicate that the vm death request
          // was removed some time between the two calls
          (mockClassUnloadManager.classUnloadRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockClassUnloadManager.createClassUnloadRequestWithId _)
            .expects(TestRequestId + "other", uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId + "other")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ClassUnloadEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureClassUnloadProfile.onClassUnloadWithData(
          arguments: _*
        )
      }

      it("should not create a new request if the previous one still exists") {
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureClassUnloadProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockClassUnloadManager.classUnloadRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockClassUnloadManager.createClassUnloadRequestWithId _)
            .expects(TestRequestId, uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId)).once()

          (mockEventManager.addEventDataStream _)
            .expects(ClassUnloadEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureClassUnloadProfile.onClassUnloadWithData(
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureClassUnloadProfile.setRequestId(TestRequestId + "other")

          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Return collection of matching arguments to indicate that we do
          // still have the request
          val internalId = java.util.UUID.randomUUID().toString
          (mockClassUnloadManager.classUnloadRequestList _)
            .expects()
            .returning(Seq(internalId)).once()
          (mockClassUnloadManager.getClassUnloadRequestInfo _)
            .expects(internalId)
            .returning(Some(ClassUnloadRequestInfo(TestRequestId, arguments))).once()

          (mockEventManager.addEventDataStream _)
            .expects(ClassUnloadEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureClassUnloadProfile.onClassUnloadWithData(
          arguments: _*
        )
      }

      it("should remove the underlying request if all pipelines are closed") {
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureClassUnloadProfile.setRequestId(TestRequestId)

        inSequence {
          inAnyOrder {
            val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
            val uniqueIdPropertyFilter =
              UniqueIdPropertyFilter(id = TestRequestId)

            // Memoized request function first checks to make sure the cache
            // has not been invalidated underneath (first call will always be
            // empty since we have never created the request)
            (mockClassUnloadManager.classUnloadRequestList _)
              .expects()
              .returning(Nil).once()
            (mockClassUnloadManager.classUnloadRequestList _)
              .expects()
              .returning(Seq(TestRequestId)).once()

            (mockClassUnloadManager.getClassUnloadRequestInfo _)
              .expects(TestRequestId)
              .returning(Some(ClassUnloadRequestInfo(TestRequestId, arguments))).once()

            // NOTE: Expect the request to be created with a unique id
            (mockClassUnloadManager.createClassUnloadRequestWithId _)
              .expects(TestRequestId, uniqueIdProperty +: arguments)
              .returning(Success(TestRequestId)).once()

            (mockEventManager.addEventDataStream _)
              .expects(ClassUnloadEventType, Seq(uniqueIdPropertyFilter))
              .returning(Pipeline.newPipeline(
                classOf[(Event, Seq[JDIEventDataResult])]
              )).twice()
          }

          (mockClassUnloadManager.removeClassUnloadRequest _)
            .expects(TestRequestId).once()
        }

        val p1 = pureClassUnloadProfile.onClassUnloadWithData(arguments: _*)
        val p2 = pureClassUnloadProfile.onClassUnloadWithData(arguments: _*)

        p1.foreach(_.close())
        p2.foreach(_.close())
      }
    }
  }
}

