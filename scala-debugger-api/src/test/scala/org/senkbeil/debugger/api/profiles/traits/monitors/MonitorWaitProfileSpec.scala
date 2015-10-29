package org.senkbeil.debugger.api.profiles.traits.monitors

import com.sun.jdi.event.MonitorWaitEvent
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline

class MonitorWaitProfileSpec extends FunSpec with Matchers with OneInstancePerTest
  with MockFactory
{
  describe("MonitorWaitProfile") {
    describe("#onMonitorWait") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[MonitorWaitEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val pipelineWithData = Pipeline.newPipeline(
          classOf[MonitorWaitProfile#MonitorWaitEventAndData]
        )

        val monitorWaitProfile = new Object with MonitorWaitProfile {
          override def onMonitorWaitWithData(
            extraArguments: JDIArgument*
          ): Pipeline[MonitorWaitEventAndData, MonitorWaitEventAndData] = {
            pipelineWithData
          }
        }

        var actual: MonitorWaitEvent = null
        monitorWaitProfile
          .onMonitorWait()
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
