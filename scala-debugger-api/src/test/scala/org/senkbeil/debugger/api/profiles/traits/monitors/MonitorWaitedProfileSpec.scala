package org.senkbeil.debugger.api.profiles.traits.monitors

import com.sun.jdi.event.MonitorWaitedEvent
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline

class MonitorWaitedProfileSpec extends FunSpec with Matchers with OneInstancePerTest
  with MockFactory
{
  describe("MonitorWaitedProfile") {
    describe("#onMonitorWaited") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[MonitorWaitedEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val pipelineWithData = Pipeline.newPipeline(
          classOf[MonitorWaitedProfile#MonitorWaitedEventAndData]
        )

        val monitorWaitedProfile = new Object with MonitorWaitedProfile {
          override def onMonitorWaitedWithData(
            extraArguments: JDIArgument*
          ): Pipeline[MonitorWaitedEventAndData, MonitorWaitedEventAndData] = {
            pipelineWithData
          }
        }

        var actual: MonitorWaitedEvent = null
        monitorWaitedProfile
          .onMonitorWaited()
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
