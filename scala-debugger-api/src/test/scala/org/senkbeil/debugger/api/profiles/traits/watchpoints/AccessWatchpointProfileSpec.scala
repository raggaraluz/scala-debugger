package org.senkbeil.debugger.api.profiles.traits.watchpoints

import com.sun.jdi.event.AccessWatchpointEvent
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline

class AccessWatchpointProfileSpec extends FunSpec with Matchers with OneInstancePerTest
  with MockFactory
{
  describe("AccessWatchpointProfile") {
    describe("#onAccessWatchpoint") {
      it("should return a pipeline with the event data results filtered out for class fields") {
        val expected = mock[AccessWatchpointEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val pipelineWithData = Pipeline.newPipeline(
          classOf[AccessWatchpointProfile#AccessWatchpointEventAndData]
        )

        val accessWatchpointProfile = new Object with AccessWatchpointProfile {
          override def onAccessFieldWatchpointWithData(
            className: String,
            fieldName: String,
            extraArguments: JDIArgument*
          ): Pipeline[AccessWatchpointEventAndData, AccessWatchpointEventAndData] = {
            pipelineWithData
          }

          override def onAccessInstanceWatchpointWithData(
            instanceVarName: String,
            extraArguments: JDIArgument*
          ): Pipeline[AccessWatchpointEventAndData, AccessWatchpointEventAndData] = ???
        }

        var actual: AccessWatchpointEvent = null
        accessWatchpointProfile.onAccessFieldWatchpoint("", "")
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        pipelineWithData.process(data)

        actual should be (expected)
      }

      it("should return a pipeline with the event data results filtered out for instance variables") {
        val expected = mock[AccessWatchpointEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val pipelineWithData = Pipeline.newPipeline(
          classOf[AccessWatchpointProfile#AccessWatchpointEventAndData]
        )

        val accessWatchpointProfile = new Object with AccessWatchpointProfile {
          override def onAccessFieldWatchpointWithData(
            className: String,
            fieldName: String,
            extraArguments: JDIArgument*
          ): Pipeline[AccessWatchpointEventAndData, AccessWatchpointEventAndData] = ???

          override def onAccessInstanceWatchpointWithData(
            instanceVarName: String,
            extraArguments: JDIArgument*
          ): Pipeline[AccessWatchpointEventAndData, AccessWatchpointEventAndData] = {
            pipelineWithData
          }
        }

        var actual: AccessWatchpointEvent = null
        accessWatchpointProfile.onAccessInstanceWatchpoint("")
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
