package org.senkbeil.debugger.api.profiles.traits.watchpoints

import com.sun.jdi.event.ModificationWatchpointEvent
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline

import scala.util.{Failure, Success, Try}

class ModificationWatchpointProfileSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[ModificationWatchpointProfile#ModificationWatchpointEventAndData]
  )

  private val successModificationWatchpointProfile = new Object with ModificationWatchpointProfile {
    override def onModificationFieldWatchpointWithData(
      className: String,
      fieldName: String,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[ModificationWatchpointEventAndData]] = {
      Success(TestPipelineWithData)
    }

    override def onModificationInstanceWatchpointWithData(
      instanceVarName: String,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[ModificationWatchpointEventAndData]] = {
      Success(TestPipelineWithData)
    }
  }

  private val failModificationWatchpointProfile = new Object with ModificationWatchpointProfile {
    override def onModificationFieldWatchpointWithData(
      className: String,
      fieldName: String,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[ModificationWatchpointEventAndData]] = {
      Failure(TestThrowable)
    }

    override def onModificationInstanceWatchpointWithData(
      instanceVarName: String,
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[ModificationWatchpointEventAndData]] = {
      Failure(TestThrowable)
    }
  }

  describe("ModificationWatchpointProfile") {
    describe("#onModificationFieldWatchpoint") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[ModificationWatchpointEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: ModificationWatchpointEvent = null
        successModificationWatchpointProfile
          .onModificationFieldWatchpoint("", "")
          .get
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should capture any error as a failure") {
        val expected = TestThrowable

        var actual: Throwable = null
        failModificationWatchpointProfile
          .onModificationFieldWatchpoint("", "")
          .failed
          .foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#onUnsafeModificationFieldWatchpoint") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[ModificationWatchpointEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: ModificationWatchpointEvent = null
        successModificationWatchpointProfile
          .onUnsafeModificationFieldWatchpoint("", "")
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw an error if it occurs") {
        intercept[Throwable] {
          failModificationWatchpointProfile.onUnsafeModificationFieldWatchpoint("", "")
        }
      }
    }

    describe("#onUnsafeModificationFieldWatchpointWithData") {
      it("should return a pipeline with the event data results") {
        // Data to be run through pipeline
        val expected = (mock[ModificationWatchpointEvent], Seq(mock[JDIEventDataResult]))

        var actual: (ModificationWatchpointEvent, Seq[JDIEventDataResult]) = null
        successModificationWatchpointProfile
          .onUnsafeModificationFieldWatchpointWithData("", "")
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw an error if it occurs") {
        intercept[Throwable] {
          failModificationWatchpointProfile
            .onUnsafeModificationFieldWatchpointWithData("", "")
        }
      }
    }

    describe("#onModificationInstanceWatchpoint") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[ModificationWatchpointEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: ModificationWatchpointEvent = null
        successModificationWatchpointProfile
          .onModificationInstanceWatchpoint("")
          .get
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should capture any error as a failure") {
        val expected = TestThrowable

        var actual: Throwable = null
        failModificationWatchpointProfile
          .onModificationInstanceWatchpoint("")
          .failed
          .foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#onUnsafeModificationInstanceWatchpoint") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[ModificationWatchpointEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: ModificationWatchpointEvent = null
        successModificationWatchpointProfile
          .onUnsafeModificationInstanceWatchpoint("")
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw an error if it occurs") {
        intercept[Throwable] {
          failModificationWatchpointProfile.onUnsafeModificationInstanceWatchpoint("")
        }
      }
    }

    describe("#onUnsafeModificationInstanceWatchpointWithData") {
      it("should return a pipeline with the event data results") {
        // Data to be run through pipeline
        val expected = (mock[ModificationWatchpointEvent], Seq(mock[JDIEventDataResult]))

        var actual: (ModificationWatchpointEvent, Seq[JDIEventDataResult]) = null
        successModificationWatchpointProfile
          .onUnsafeModificationInstanceWatchpointWithData("")
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw an error if it occurs") {
        intercept[Throwable] {
          failModificationWatchpointProfile
            .onUnsafeModificationInstanceWatchpointWithData("")
        }
      }
    }
  }
}

