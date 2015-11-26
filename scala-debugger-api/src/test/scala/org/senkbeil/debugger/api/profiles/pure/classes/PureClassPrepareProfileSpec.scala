package org.senkbeil.debugger.api.profiles.pure.classes

import com.sun.jdi.event.{Event, EventQueue}
import com.sun.jdi.request.EventRequestManager
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.classes.ClassPrepareManager
import org.senkbeil.debugger.api.lowlevel.events.EventManager
import org.senkbeil.debugger.api.lowlevel.events.EventType.ClassPrepareEventType
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.utils.LoopingTaskRunner
import test.JDIMockHelpers

import scala.util.{Failure, Success}

class PureClassPrepareProfileSpec extends FunSpec with Matchers
with OneInstancePerTest with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString

  // Workaround - see https://github.com/paulbutcher/ScalaMock/issues/33
  private class ZeroArgClassPrepareManager extends ClassPrepareManager(
    stub[EventRequestManager]
  )
  private val mockClassPrepareManager = mock[ZeroArgClassPrepareManager]

  // Workaround - see https://github.com/paulbutcher/ScalaMock/issues/33
  private class ZeroArgEventManager extends EventManager(
    stub[EventQueue],
    stub[LoopingTaskRunner],
    autoStart = false
  )
  private val mockEventManager = mock[ZeroArgEventManager]

  private val pureClassPrepareProfile = new Object with PureClassPrepareProfile {
    private var requestId: String = _
    def setRequestId(requestId: String): Unit = this.requestId = requestId

    // NOTE: If we set a specific request id, return that, otherwise use the
    //       default behavior
    override protected def newClassPrepareRequestId(): String =
      if (requestId != null) requestId else super.newClassPrepareRequestId()

    override protected val classPrepareManager = mockClassPrepareManager
    override protected val eventManager: EventManager = mockEventManager
  }

  describe("PureClassPrepareProfile") {
    describe("#onClassPrepareWithData") {
      it("should create a new request if one has not be made yet") {
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
        val uniqueIdPropertyFilter = UniqueIdPropertyFilter(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureClassPrepareProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockClassPrepareManager.classPrepareRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockClassPrepareManager.createClassPrepareRequestWithId _)
            .expects(TestRequestId, uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId)).once()

          (mockEventManager.addEventDataStream _)
            .expects(ClassPrepareEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureClassPrepareProfile.onClassPrepareWithData(
          arguments: _*
        )
      }

      it("should capture exceptions thrown when creating the request") {
        val expected = Failure(new Throwable)
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureClassPrepareProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockClassPrepareManager.classPrepareRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockClassPrepareManager.createClassPrepareRequestWithId _)
            .expects(TestRequestId, uniqueIdProperty +: arguments)
            .throwing(expected.failed.get).once()
        }

        val actual = pureClassPrepareProfile.onClassPrepareWithData(
          arguments: _*
        )

        actual should be (expected)
      }

      it("should create a new request if the previous one was removed") {
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureClassPrepareProfile.setRequestId(TestRequestId)

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureClassPrepareProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockClassPrepareManager.classPrepareRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockClassPrepareManager.createClassPrepareRequestWithId _)
            .expects(TestRequestId, uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId)).once()

          (mockEventManager.addEventDataStream _)
            .expects(ClassPrepareEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureClassPrepareProfile.onClassPrepareWithData(
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureClassPrepareProfile.setRequestId(TestRequestId + "other")

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId + "other")
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId + "other")

          // Return empty this time to indicate that the vm death request
          // was removed some time between the two calls
          (mockClassPrepareManager.classPrepareRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockClassPrepareManager.createClassPrepareRequestWithId _)
            .expects(TestRequestId + "other", uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId + "other")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ClassPrepareEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureClassPrepareProfile.onClassPrepareWithData(
          arguments: _*
        )
      }

      it("should not create a new request if the previous one still exists") {
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureClassPrepareProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // empty since we have never created the request)
          (mockClassPrepareManager.classPrepareRequestList _)
            .expects()
            .returning(Nil).once()

          // NOTE: Expect the request to be created with a unique id
          (mockClassPrepareManager.createClassPrepareRequestWithId _)
            .expects(TestRequestId, uniqueIdProperty +: arguments)
            .returning(Success(TestRequestId)).once()

          (mockEventManager.addEventDataStream _)
            .expects(ClassPrepareEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureClassPrepareProfile.onClassPrepareWithData(
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureClassPrepareProfile.setRequestId(TestRequestId + "other")

          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Return collection of matching arguments to indicate that we do
          // still have the request
          val internalId = java.util.UUID.randomUUID().toString
          (mockClassPrepareManager.classPrepareRequestList _)
            .expects()
            .returning(Seq(internalId)).once()
          (mockClassPrepareManager.getClassPrepareRequestArguments _)
            .expects(internalId)
            .returning(Some(arguments)).once()

          (mockEventManager.addEventDataStream _)
            .expects(ClassPrepareEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureClassPrepareProfile.onClassPrepareWithData(
          arguments: _*
        )
      }

      it("should remove the underlying request if all pipelines are closed") {
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureClassPrepareProfile.setRequestId(TestRequestId)

        inSequence {
          inAnyOrder {
            val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
            val uniqueIdPropertyFilter =
              UniqueIdPropertyFilter(id = TestRequestId)

            // Memoized request function first checks to make sure the cache
            // has not been invalidated underneath (first call will always be
            // empty since we have never created the request)
            (mockClassPrepareManager.classPrepareRequestList _)
              .expects()
              .returning(Nil).once()
            (mockClassPrepareManager.classPrepareRequestList _)
              .expects()
              .returning(Seq(TestRequestId)).once()

            (mockClassPrepareManager.getClassPrepareRequestArguments _)
              .expects(TestRequestId)
              .returning(Some(arguments)).once()

            // NOTE: Expect the request to be created with a unique id
            (mockClassPrepareManager.createClassPrepareRequestWithId _)
              .expects(TestRequestId, uniqueIdProperty +: arguments)
              .returning(Success(TestRequestId)).once()

            (mockEventManager.addEventDataStream _)
              .expects(ClassPrepareEventType, Seq(uniqueIdPropertyFilter))
              .returning(Pipeline.newPipeline(
                classOf[(Event, Seq[JDIEventDataResult])]
              )).twice()
          }

          (mockClassPrepareManager.removeClassPrepareRequest _)
            .expects(TestRequestId).once()
        }

        val p1 = pureClassPrepareProfile.onClassPrepareWithData(arguments: _*)
        val p2 = pureClassPrepareProfile.onClassPrepareWithData(arguments: _*)

        p1.foreach(_.close())
        p2.foreach(_.close())
      }
    }
  }
}

