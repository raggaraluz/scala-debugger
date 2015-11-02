package org.senkbeil.debugger.api.profiles.traits.breakpoints

import com.sun.jdi.event.BreakpointEvent
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline

class BreakpointProfileSpec extends FunSpec with Matchers with OneInstancePerTest
  with MockFactory
{
  describe("BreakpointProfile") {
    describe("#onBreakpoint") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[BreakpointEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val pipelineWithData = Pipeline.newPipeline(
          classOf[BreakpointProfile#BreakpointEventAndData]
        )

        val breakpointProfile = new Object with BreakpointProfile {
          override def onBreakpointWithData(
            fileName: String,
            lineNumber: Int,
            extraArguments: JDIArgument*
          ): IdentityPipeline[BreakpointEventAndData] = {
            pipelineWithData
          }
        }

        var actual: BreakpointEvent = null
        breakpointProfile.onBreakpoint("", 0).foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        pipelineWithData.process(data)

        actual should be (expected)
      }
    }
  }
}
