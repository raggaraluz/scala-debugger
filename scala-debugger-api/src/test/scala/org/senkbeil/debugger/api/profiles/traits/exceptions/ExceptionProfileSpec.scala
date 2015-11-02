package org.senkbeil.debugger.api.profiles.traits.exceptions

import com.sun.jdi.event.ExceptionEvent
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline

class ExceptionProfileSpec extends FunSpec with Matchers with OneInstancePerTest
  with MockFactory
{
  describe("ExceptionProfile") {
    describe("#onException") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[ExceptionEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val pipelineWithData = Pipeline.newPipeline(
          classOf[ExceptionProfile#ExceptionEventAndData]
        )

        val exceptionProfile = new Object with ExceptionProfile {
          override def onExceptionWithData(
            exceptionName: String,
            notifyCaught: Boolean,
            notifyUncaught: Boolean,
            extraArguments: JDIArgument*
          ): IdentityPipeline[ExceptionEventAndData] = {
            pipelineWithData
          }

          override def onAllExceptionsWithData(
            notifyCaught: Boolean,
            notifyUncaught: Boolean,
            extraArguments: JDIArgument*
          ): IdentityPipeline[ExceptionEventAndData] = ???
        }

        var actual: ExceptionEvent = null
        exceptionProfile.onException("", true, true).foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        pipelineWithData.process(data)

        actual should be (expected)
      }
    }

    describe("#onAllExceptions") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[ExceptionEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val pipelineWithData = Pipeline.newPipeline(
          classOf[ExceptionProfile#ExceptionEventAndData]
        )

        val exceptionProfile = new Object with ExceptionProfile {
          override def onExceptionWithData(
            exceptionName: String,
            notifyCaught: Boolean,
            notifyUncaught: Boolean,
            extraArguments: JDIArgument*
          ): IdentityPipeline[ExceptionEventAndData] = ???

          override def onAllExceptionsWithData(
            notifyCaught: Boolean,
            notifyUncaught: Boolean,
            extraArguments: JDIArgument*
          ): IdentityPipeline[ExceptionEventAndData] = {
            pipelineWithData
          }
        }

        var actual: ExceptionEvent = null
        exceptionProfile.onAllExceptions(true, true).foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        pipelineWithData.process(data)

        actual should be (expected)
      }
    }
  }
}
