package org.senkbeil.debugger.api.profiles.traits.vm

import com.sun.jdi.event.VMStartEvent
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline

class VMStartProfileSpec extends FunSpec with Matchers with OneInstancePerTest
  with MockFactory
{
  describe("VMStartProfile") {
    describe("#onVMStart") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[VMStartEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val pipelineWithData = Pipeline.newPipeline(
          classOf[VMStartProfile#VMStartEventAndData]
        )

        val vmStartProfile = new Object with VMStartProfile {
          override def onVMStartWithData(
            extraArguments: JDIArgument*
          ): IdentityPipeline[VMStartEventAndData] = {
            pipelineWithData
          }
        }

        var actual: VMStartEvent = null
        vmStartProfile.onVMStart().foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        pipelineWithData.process(data)

        actual should be (expected)
      }
    }
  }
}
