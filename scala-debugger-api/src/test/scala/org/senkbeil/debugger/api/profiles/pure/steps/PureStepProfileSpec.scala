package org.senkbeil.debugger.api.profiles.pure.steps

import com.sun.jdi.ThreadReference
import com.sun.jdi.event.{StepEvent, Event, EventQueue}
import com.sun.jdi.request.EventRequestManager
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.{ScalaFutures, Futures}
import org.scalatest.time.{Span, Milliseconds}
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.lowlevel.requests.filters.ThreadFilter
import org.senkbeil.debugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.senkbeil.debugger.api.lowlevel.steps.StepManager
import org.senkbeil.debugger.api.lowlevel.events.{JDIEventArgument, EventManager}
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.pipelines.{Operation, Pipeline}
import org.senkbeil.debugger.api.utils.LoopingTaskRunner
import org.senkbeil.debugger.api.lowlevel.events.EventType.StepEventType
import test.JDIMockHelpers

import scala.util.{Failure, Success}

class PureStepProfileSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory with JDIMockHelpers with Futures
  with ScalaFutures
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(3000, Milliseconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockThreadReference = mock[ThreadReference]

  // Workaround - see https://github.com/paulbutcher/ScalaMock/issues/33
  private class ZeroArgStepManager extends StepManager(
    stub[EventRequestManager]
  )
  private val mockStepManager = mock[ZeroArgStepManager]

  // Workaround - see https://github.com/paulbutcher/ScalaMock/issues/33
  private class ZeroArgEventManager extends EventManager(
    stub[EventQueue],
    stub[LoopingTaskRunner],
    autoStart = false
  )
  private val mockEventManager = mock[ZeroArgEventManager]

  private val pureStepProfile = new Object with PureStepProfile {
    override protected val stepManager = mockStepManager
    override protected val eventManager = mockEventManager
  }

  describe("PureStepProfile") {
    describe("#stepIntoLineWithData") {
      it("should create a new step request and pipeline whose future is returned") {
        val expected = (mock[StepEvent], Nil)

        val stepPipeline = Pipeline.newPipeline(
          classOf[(Event, Seq[JDIEventDataResult])]
        )
        val rArgs = Seq(mock[JDIRequestArgument])
        val eArgs = Seq(mock[JDIEventArgument])

        // These filters should be injected by our profile
        val threadFilter = ThreadFilter(threadReference = mockThreadReference)

        (mockStepManager.createStepIntoLineRequest _)
          .expects(mockThreadReference, rArgs :+ threadFilter)
          .returning(Success(TestRequestId)).once()

        (mockEventManager.addEventDataStream _)
          .expects(StepEventType, eArgs)
          .returning(stepPipeline).once()

        val stepFuture = pureStepProfile.stepIntoLineWithData(
          mockThreadReference,
          rArgs ++ eArgs: _*
        )

        // Process the pipeline to trigger the future
        stepPipeline.process(expected)

        whenReady(stepFuture) { actual => actual should be (expected) }
      }

      it("should capture exceptions thrown when creating the request") {
        val expected = new Throwable

        (mockStepManager.createStepIntoLineRequest _).expects(*, *)
          .returning(Failure(expected)).once()

        val stepFuture = pureStepProfile.stepIntoLineWithData(
          mockThreadReference
        )

        whenReady(stepFuture.failed) { actual => actual should be (expected) }
      }
    }

    describe("#stepOutLineWithData") {
      it("should create a new step request and pipeline whose future is returned") {
        val expected = (mock[StepEvent], Nil)

        val stepPipeline = Pipeline.newPipeline(
          classOf[(Event, Seq[JDIEventDataResult])]
        )
        val rArgs = Seq(mock[JDIRequestArgument])
        val eArgs = Seq(mock[JDIEventArgument])

        // These filters should be injected by our profile
        val threadFilter = ThreadFilter(threadReference = mockThreadReference)

        (mockStepManager.createStepOutLineRequest _)
          .expects(mockThreadReference, rArgs :+ threadFilter)
          .returning(Success(TestRequestId)).once()

        (mockEventManager.addEventDataStream _)
          .expects(StepEventType, eArgs)
          .returning(stepPipeline).once()

        val stepFuture = pureStepProfile.stepOutLineWithData(
          mockThreadReference,
          rArgs ++ eArgs: _*
        )

        // Process the pipeline to trigger the future
        stepPipeline.process(expected)

        whenReady(stepFuture) { actual => actual should be (expected) }
      }

      it("should capture exceptions thrown when creating the request") {
        val expected = new Throwable

        (mockStepManager.createStepOutLineRequest _).expects(*, *)
          .returning(Failure(expected)).once()

        val stepFuture = pureStepProfile.stepOutLineWithData(
          mockThreadReference
        )

        whenReady(stepFuture.failed) { actual => actual should be (expected) }
      }
    }

    describe("#stepOverLineWithData") {
      it("should create a new step request and pipeline whose future is returned") {
        val expected = (mock[StepEvent], Nil)

        val stepPipeline = Pipeline.newPipeline(
          classOf[(Event, Seq[JDIEventDataResult])]
        )
        val rArgs = Seq(mock[JDIRequestArgument])
        val eArgs = Seq(mock[JDIEventArgument])

        // These filters should be injected by our profile
        val threadFilter = ThreadFilter(threadReference = mockThreadReference)

        (mockStepManager.createStepOverLineRequest _)
          .expects(mockThreadReference, rArgs :+ threadFilter)
          .returning(Success(TestRequestId)).once()

        (mockEventManager.addEventDataStream _)
          .expects(StepEventType, eArgs)
          .returning(stepPipeline).once()

        val stepFuture = pureStepProfile.stepOverLineWithData(
          mockThreadReference,
          rArgs ++ eArgs: _*
        )

        // Process the pipeline to trigger the future
        stepPipeline.process(expected)

        whenReady(stepFuture) { actual => actual should be (expected) }
      }

      it("should capture exceptions thrown when creating the request") {
        val expected = new Throwable

        (mockStepManager.createStepOverLineRequest _).expects(*, *)
          .returning(Failure(expected)).once()

        val stepFuture = pureStepProfile.stepOverLineWithData(
          mockThreadReference
        )

        whenReady(stepFuture.failed) { actual => actual should be (expected) }
      }
    }

    describe("#stepIntoMinWithData") {
      it("should create a new step request and pipeline whose future is returned") {
        val expected = (mock[StepEvent], Nil)

        val stepPipeline = Pipeline.newPipeline(
          classOf[(Event, Seq[JDIEventDataResult])]
        )
        val rArgs = Seq(mock[JDIRequestArgument])
        val eArgs = Seq(mock[JDIEventArgument])

        // These filters should be injected by our profile
        val threadFilter = ThreadFilter(threadReference = mockThreadReference)

        (mockStepManager.createStepIntoMinRequest _)
          .expects(mockThreadReference, rArgs :+ threadFilter)
          .returning(Success(TestRequestId)).once()

        (mockEventManager.addEventDataStream _)
          .expects(StepEventType, eArgs)
          .returning(stepPipeline).once()

        val stepFuture = pureStepProfile.stepIntoMinWithData(
          mockThreadReference,
          rArgs ++ eArgs: _*
        )

        // Process the pipeline to trigger the future
        stepPipeline.process(expected)

        whenReady(stepFuture) { actual => actual should be (expected) }
      }

      it("should capture exceptions thrown when creating the request") {
        val expected = new Throwable

        (mockStepManager.createStepIntoMinRequest _).expects(*, *)
          .returning(Failure(expected)).once()

        val stepFuture = pureStepProfile.stepIntoMinWithData(
          mockThreadReference
        )

        whenReady(stepFuture.failed) { actual => actual should be (expected) }
      }
    }

    describe("#stepOutMinWithData") {
      it("should create a new step request and pipeline whose future is returned") {
        val expected = (mock[StepEvent], Nil)

        val stepPipeline = Pipeline.newPipeline(
          classOf[(Event, Seq[JDIEventDataResult])]
        )
        val rArgs = Seq(mock[JDIRequestArgument])
        val eArgs = Seq(mock[JDIEventArgument])

        // These filters should be injected by our profile
        val threadFilter = ThreadFilter(threadReference = mockThreadReference)

        (mockStepManager.createStepOutMinRequest _)
          .expects(mockThreadReference, rArgs :+ threadFilter)
          .returning(Success(TestRequestId)).once()

        (mockEventManager.addEventDataStream _)
          .expects(StepEventType, eArgs)
          .returning(stepPipeline).once()

        val stepFuture = pureStepProfile.stepOutMinWithData(
          mockThreadReference,
          rArgs ++ eArgs: _*
        )

        // Process the pipeline to trigger the future
        stepPipeline.process(expected)

        whenReady(stepFuture) { actual => actual should be (expected) }
      }

      it("should capture exceptions thrown when creating the request") {
        val expected = new Throwable

        (mockStepManager.createStepOutMinRequest _).expects(*, *)
          .returning(Failure(expected)).once()

        val stepFuture = pureStepProfile.stepOutMinWithData(
          mockThreadReference
        )

        whenReady(stepFuture.failed) { actual => actual should be (expected) }
      }
    }

    describe("#stepOverMinWithData") {
      it("should create a new step request and pipeline whose future is returned") {
        val expected = (mock[StepEvent], Nil)

        val stepPipeline = Pipeline.newPipeline(
          classOf[(Event, Seq[JDIEventDataResult])]
        )
        val rArgs = Seq(mock[JDIRequestArgument])
        val eArgs = Seq(mock[JDIEventArgument])

        // These filters should be injected by our profile
        val threadFilter = ThreadFilter(threadReference = mockThreadReference)

        (mockStepManager.createStepOverMinRequest _)
          .expects(mockThreadReference, rArgs :+ threadFilter)
          .returning(Success(TestRequestId)).once()

        (mockEventManager.addEventDataStream _)
          .expects(StepEventType, eArgs)
          .returning(stepPipeline).once()

        val stepFuture = pureStepProfile.stepOverMinWithData(
          mockThreadReference,
          rArgs ++ eArgs: _*
        )

        // Process the pipeline to trigger the future
        stepPipeline.process(expected)

        whenReady(stepFuture) { actual => actual should be (expected) }
      }

      it("should capture exceptions thrown when creating the request") {
        val expected = new Throwable

        (mockStepManager.createStepOverMinRequest _).expects(*, *)
          .returning(Failure(expected)).once()

        val stepFuture = pureStepProfile.stepOverMinWithData(
          mockThreadReference
        )

        whenReady(stepFuture.failed) { actual => actual should be (expected) }
      }
    }

    describe("#onStepWithData") {
      it("should create a stream of events with data for steps") {
        val expected = (mock[StepEvent], Seq(mock[JDIEventDataResult]))
        val arguments = Seq(mock[JDIEventArgument])

        (mockEventManager.addEventDataStream _).expects(
          StepEventType, arguments
        ).returning(
            Pipeline.newPipeline(classOf[(Event, Seq[JDIEventDataResult])])
              .map(t => (expected._1, expected._2))
          ).once()

        var actual: (StepEvent, Seq[JDIEventDataResult]) = null
        val pipeline =
          pureStepProfile.onStepWithData(mockThreadReference, arguments: _*)
        pipeline.get.foreach(actual = _)

        pipeline.get.process(expected)

        actual should be (expected)
      }
    }
  }
}
