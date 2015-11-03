package org.senkbeil.debugger.api.profiles.traits.threads

import com.sun.jdi.event.ThreadStartEvent
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline

import scala.util.{Failure, Success, Try}

class ThreadStartProfileSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[ThreadStartProfile#ThreadStartEventAndData]
  )

  private val successThreadStartProfile = new Object with ThreadStartProfile {
    override def onThreadStartWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[ThreadStartEventAndData]] = {
      Success(TestPipelineWithData)
    }
  }

  private val failThreadStartProfile = new Object with ThreadStartProfile {
    override def onThreadStartWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[ThreadStartEventAndData]] = {
      Failure(TestThrowable)
    }
  }

  describe("ThreadStartProfile") {
    describe("#onThreadStart") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[ThreadStartEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: ThreadStartEvent = null
        successThreadStartProfile.onThreadStart().get.foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should capture any exception as a failure") {
        val expected = TestThrowable

        // Data to be run through pipeline
        val data = (mock[ThreadStartEvent], Seq(mock[JDIEventDataResult]))

        var actual: Throwable = null
        failThreadStartProfile.onThreadStart().failed.foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#onUnsafeThreadStart") {
      it("should return a pipeline of events if successful") {
        val expected = mock[ThreadStartEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: ThreadStartEvent = null
        successThreadStartProfile.onUnsafeThreadStart().foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failThreadStartProfile.onUnsafeThreadStart()
        }
      }
    }

    describe("#onUnsafeThreadStartWithData") {
      it("should return a pipeline of events and data if successful") {
        // Data to be run through pipeline
        val expected = (mock[ThreadStartEvent], Seq(mock[JDIEventDataResult]))

        var actual: (ThreadStartEvent, Seq[JDIEventDataResult]) = null
        successThreadStartProfile
          .onUnsafeThreadStartWithData()
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failThreadStartProfile.onUnsafeThreadStartWithData()
        }
      }
    }
  }
}

