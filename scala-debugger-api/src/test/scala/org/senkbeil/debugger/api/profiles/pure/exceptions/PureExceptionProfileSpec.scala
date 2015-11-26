package org.senkbeil.debugger.api.profiles.pure.exceptions

import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.{Event, EventQueue}
import com.sun.jdi.request.EventRequestManager
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.senkbeil.debugger.api.lowlevel.exceptions.ExceptionManager
import org.senkbeil.debugger.api.lowlevel.events.EventManager
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.utils.LoopingTaskRunner
import test.JDIMockHelpers

import org.senkbeil.debugger.api.lowlevel.events.EventType.ExceptionEventType
import scala.util.{Failure, Success}

class PureExceptionProfileSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString

  // Workaround - see https://github.com/paulbutcher/ScalaMock/issues/33
  private class ZeroArgExceptionManager extends ExceptionManager(
    stub[VirtualMachine],
    stub[EventRequestManager]
  )
  private val mockExceptionManager = mock[ZeroArgExceptionManager]

  // Workaround - see https://github.com/paulbutcher/ScalaMock/issues/33
  private class ZeroArgEventManager extends EventManager(
    stub[EventQueue],
    stub[LoopingTaskRunner],
    autoStart = false
  )
  private val mockEventManager = mock[ZeroArgEventManager]

  private val pureExceptionProfile = new Object with PureExceptionProfile {
    private var requestId: String = _
    def setRequestId(requestId: String): Unit = this.requestId = requestId

    // NOTE: If we set a specific request id, return that, otherwise use the
    //       default behavior
    override protected def newExceptionRequestId(): String =
      if (requestId != null) requestId else super.newExceptionRequestId()

    override protected val exceptionManager = mockExceptionManager
    override protected val eventManager: EventManager = mockEventManager
  }

  describe("PureExceptionProfile") {
    describe("#onExceptionWithData") {
      it("should create a new request if one has not be made yet") {
        val exceptionName = "some.exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
        val uniqueIdPropertyFilter = UniqueIdPropertyFilter(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureExceptionProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockExceptionManager.hasExceptionRequest _)
            .expects(exceptionName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createExceptionRequestWithId _).expects(
            TestRequestId,
            exceptionName,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.onExceptionWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
      }

      it("should capture exceptions thrown when creating the request") {
        val expected = Failure(new Throwable)
        val exceptionName = "some.exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureExceptionProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockExceptionManager.hasExceptionRequest _)
            .expects(exceptionName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createExceptionRequestWithId _).expects(
            TestRequestId,
            exceptionName,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).throwing(expected.failed.get).once()
        }

        val actual = pureExceptionProfile.onExceptionWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        actual should be (expected)
      }

      it("should create a new request if the previous one was removed") {
        val exceptionName = "some.exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureExceptionProfile.setRequestId(TestRequestId)

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureExceptionProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockExceptionManager.hasExceptionRequest _)
            .expects(exceptionName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createExceptionRequestWithId _).expects(
            TestRequestId,
            exceptionName,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.onExceptionWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureExceptionProfile.setRequestId(TestRequestId + "other")

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId + "other")
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId + "other")

          // Return false this time to indicate that the exception request
          // was removed some time between the two calls
          (mockExceptionManager.hasExceptionRequest _)
            .expects(exceptionName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createExceptionRequestWithId _).expects(
            TestRequestId + "other",
            exceptionName,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.onExceptionWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
      }

      it("should not create a new request if the previous one still exists") {
        val exceptionName = "some.exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureExceptionProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockExceptionManager.hasExceptionRequest _)
            .expects(exceptionName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createExceptionRequestWithId _).expects(
            TestRequestId,
            exceptionName,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.onExceptionWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureExceptionProfile.setRequestId(TestRequestId + "other")

          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Return true to indicate that we do still have the request
          (mockExceptionManager.hasExceptionRequest _)
            .expects(exceptionName)
            .returning(true).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.onExceptionWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
      }

      it("should create a new request for different input") {
        val exceptionName = "some.exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureExceptionProfile.setRequestId(TestRequestId)

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureExceptionProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockExceptionManager.hasExceptionRequest _)
            .expects(exceptionName)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createExceptionRequestWithId _).expects(
            TestRequestId,
            exceptionName,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.onExceptionWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureExceptionProfile.setRequestId(TestRequestId + "other")

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId + "other")
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId + "other")

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockExceptionManager.hasExceptionRequest _)
            .expects(exceptionName + 1)
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createExceptionRequestWithId _).expects(
            TestRequestId + "other",
            exceptionName + 1,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.onExceptionWithData(
          exceptionName + 1,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
      }

      it("should remove the underlying request if all pipelines are closed") {
        val exceptionName = "some.exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureExceptionProfile.setRequestId(TestRequestId)

        inSequence {
          inAnyOrder {
            val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
            val uniqueIdPropertyFilter =
              UniqueIdPropertyFilter(id = TestRequestId)

            // Memoized request function first checks to make sure the cache
            // has not been invalidated underneath (first call will always be
            // empty since we have never created the request)
            (mockExceptionManager.hasExceptionRequest _).expects(exceptionName)
              .returning(false).once()
            (mockExceptionManager.hasExceptionRequest _).expects(exceptionName)
              .returning(true).once()

            // NOTE: Expect the request to be created with a unique id
            (mockExceptionManager.createExceptionRequestWithId _).expects(
              TestRequestId,
              exceptionName,
              notifyCaught,
              notifyUncaught,
              uniqueIdProperty +: arguments
            ).returning(Success("")).once()

            (mockEventManager.addEventDataStream _)
              .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
              .returning(Pipeline.newPipeline(
                classOf[(Event, Seq[JDIEventDataResult])]
              )).twice()
          }

          (mockExceptionManager.removeExceptionRequestWithId _)
            .expects(TestRequestId).once()
        }

        val p1 = pureExceptionProfile.onExceptionWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
        val p2 = pureExceptionProfile.onExceptionWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        p1.foreach(_.close())
        p2.foreach(_.close())
      }
    }

    describe("#onAllExceptionsWithData") {
      it("should create a new request if one has not be made yet") {
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
        val uniqueIdPropertyFilter = UniqueIdPropertyFilter(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureExceptionProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockExceptionManager.hasCatchallExceptionRequest _)
            .expects()
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createCatchallExceptionRequestWithId _).expects(
            TestRequestId,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.onAllExceptionsWithData(
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
      }

      it("should capture exceptions thrown when creating the request") {
        val expected = Failure(new Throwable)
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureExceptionProfile.setRequestId(TestRequestId)

        inSequence {
          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockExceptionManager.hasCatchallExceptionRequest _)
            .expects()
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createCatchallExceptionRequestWithId _).expects(
            TestRequestId,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).throwing(expected.failed.get).once()
        }

        val actual = pureExceptionProfile.onAllExceptionsWithData(
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        actual should be (expected)
      }

      it("should create a new request if the previous one was removed") {
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureExceptionProfile.setRequestId(TestRequestId)

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureExceptionProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockExceptionManager.hasCatchallExceptionRequest _)
            .expects()
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createCatchallExceptionRequestWithId _).expects(
            TestRequestId,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.onAllExceptionsWithData(
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureExceptionProfile.setRequestId(TestRequestId + "other")

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId + "other")
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId + "other")

          // Return false this time to indicate that the exception request
          // was removed some time between the two calls
          (mockExceptionManager.hasCatchallExceptionRequest _)
            .expects()
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createCatchallExceptionRequestWithId _).expects(
            TestRequestId + "other",
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.onAllExceptionsWithData(
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
      }

      it("should not create a new request if the previous one still exists") {
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureExceptionProfile.setRequestId(TestRequestId)

          val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Memoized request function first checks to make sure the cache
          // has not been invalidated underneath (first call will always be
          // false since we have never created the request)
          (mockExceptionManager.hasCatchallExceptionRequest _)
            .expects()
            .returning(false).once()

          // NOTE: Expect the request to be created with a unique id
          (mockExceptionManager.createCatchallExceptionRequestWithId _).expects(
            TestRequestId,
            notifyCaught,
            notifyUncaught,
            uniqueIdProperty +: arguments
          ).returning(Success("")).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.onAllExceptionsWithData(
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        inSequence {
          // Set a known test id so we can validate the unique property is added
          import scala.language.reflectiveCalls
          pureExceptionProfile.setRequestId(TestRequestId + "other")

          val uniqueIdPropertyFilter =
            UniqueIdPropertyFilter(id = TestRequestId)

          // Return true to indicate that we do still have the request
          (mockExceptionManager.hasCatchallExceptionRequest _)
            .expects()
            .returning(true).once()

          (mockEventManager.addEventDataStream _)
            .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
            .returning(Pipeline.newPipeline(
              classOf[(Event, Seq[JDIEventDataResult])]
            )).once()
        }

        pureExceptionProfile.onAllExceptionsWithData(
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
      }

      it("should remove the underlying request if all pipelines are closed") {
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIRequestArgument])

        // Set a known test id so we can validate the unique property is added
        import scala.language.reflectiveCalls
        pureExceptionProfile.setRequestId(TestRequestId)

        inSequence {
          inAnyOrder {
            val uniqueIdProperty = UniqueIdProperty(id = TestRequestId)
            val uniqueIdPropertyFilter =
              UniqueIdPropertyFilter(id = TestRequestId)

            // Memoized request function first checks to make sure the cache
            // has not been invalidated underneath (first call will always be
            // empty since we have never created the request)
            (mockExceptionManager.hasCatchallExceptionRequest _).expects()
              .returning(false).once()
            (mockExceptionManager.hasCatchallExceptionRequest _).expects()
              .returning(true).once()

            // NOTE: Expect the request to be created with a unique id
            (mockExceptionManager.createCatchallExceptionRequestWithId _)
              .expects(
                TestRequestId,
                notifyCaught,
                notifyUncaught,
                uniqueIdProperty +: arguments
              )
              .returning(Success("")).once()

            (mockEventManager.addEventDataStream _)
              .expects(ExceptionEventType, Seq(uniqueIdPropertyFilter))
              .returning(Pipeline.newPipeline(
                classOf[(Event, Seq[JDIEventDataResult])]
              )).twice()
          }

          (mockExceptionManager.removeExceptionRequestWithId _)
            .expects(TestRequestId).once()
        }

        val p1 = pureExceptionProfile.onAllExceptionsWithData(
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
        val p2 = pureExceptionProfile.onAllExceptionsWithData(
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )

        p1.foreach(_.close())
        p2.foreach(_.close())
      }
    }
  }
}
