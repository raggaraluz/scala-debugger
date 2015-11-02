package org.senkbeil.debugger.api.profiles.traits.monitors

import com.sun.jdi.event.MonitorContendedEnterEvent
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline

class MonitorContendedEnterProfileSpec extends FunSpec with Matchers with OneInstancePerTest
  with MockFactory
{
  describe("MonitorContendedEnterProfile") {
    describe("#onMonitorContendedEnter") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[MonitorContendedEnterEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val pipelineWithData = Pipeline.newPipeline(
          classOf[MonitorContendedEnterProfile#MonitorContendedEnterEventAndData]
        )

        val monitorContendedEnterProfile = new Object with MonitorContendedEnterProfile {
          override def onMonitorContendedEnterWithData(
            extraArguments: JDIArgument*
          ): IdentityPipeline[MonitorContendedEnterEventAndData] = {
            pipelineWithData
          }
        }

        var actual: MonitorContendedEnterEvent = null
        monitorContendedEnterProfile
          .onMonitorContendedEnter()
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        pipelineWithData.process(data)

        actual should be (expected)
      }
    }
  }
}
