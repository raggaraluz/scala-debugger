package org.senkbeil.debugger.api.profiles.traits.events

import com.sun.jdi.event.Event
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.EventType.EventType
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline

class EventProfileSpec extends FunSpec with Matchers with OneInstancePerTest
  with MockFactory
{
  describe("EventProfile") {
    describe("#onEvent") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[Event]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val pipelineWithData = Pipeline.newPipeline(
          classOf[EventProfile#EventAndData]
        )

        val eventProfile = new Object with EventProfile {
          override def onEventWithData(
            eventType: EventType,
            extraArguments: JDIArgument*
          ): IdentityPipeline[EventAndData] = {
            pipelineWithData
          }
        }

        var actual: Event = null
        eventProfile.onEvent(mock[EventType]).foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        pipelineWithData.process(data)

        actual should be (expected)
      }
    }
  }
}
