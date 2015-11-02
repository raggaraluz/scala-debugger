package org.senkbeil.debugger.api.profiles.traits.vm

import com.sun.jdi.event.VMDeathEvent
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline

class VMDeathProfileSpec extends FunSpec with Matchers with OneInstancePerTest
  with MockFactory
{
  describe("VMDeathProfile") {
    describe("#onVMDeath") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[VMDeathEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val pipelineWithData = Pipeline.newPipeline(
          classOf[VMDeathProfile#VMDeathEventAndData]
        )

        val vmDeathProfile = new Object with VMDeathProfile {
          override def onVMDeathWithData(
            extraArguments: JDIArgument*
          ): IdentityPipeline[VMDeathEventAndData] = {
            pipelineWithData
          }
        }

        var actual: VMDeathEvent = null
        vmDeathProfile.onVMDeath().foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        pipelineWithData.process(data)

        actual should be (expected)
      }
    }
  }
}
