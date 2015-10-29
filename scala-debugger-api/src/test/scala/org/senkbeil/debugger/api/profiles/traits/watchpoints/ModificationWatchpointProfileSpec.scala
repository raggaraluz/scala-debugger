package org.senkbeil.debugger.api.profiles.traits.watchpoints

import com.sun.jdi.event.ModificationWatchpointEvent
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline

class ModificationWatchpointProfileSpec extends FunSpec with Matchers with OneInstancePerTest
  with MockFactory
{
  describe("ModificationWatchpointProfile") {
    describe("#onModificationWatchpoint") {
      it("should return a pipeline with the event data results filtered out for class fields") {
        val expected = mock[ModificationWatchpointEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val pipelineWithData = Pipeline.newPipeline(
          classOf[ModificationWatchpointProfile#ModificationWatchpointEventAndData]
        )

        val modificationWatchpointProfile = new Object with ModificationWatchpointProfile {
          override def onModificationFieldWatchpointWithData(
            className: String,
            fieldName: String,
            extraArguments: JDIArgument*
          ): Pipeline[ModificationWatchpointEventAndData, ModificationWatchpointEventAndData] = {
            pipelineWithData
          }

          override def onModificationInstanceWatchpointWithData(
            instanceVarName: String,
            extraArguments: JDIArgument*
          ): Pipeline[ModificationWatchpointEventAndData, ModificationWatchpointEventAndData] = ???
        }

        var actual: ModificationWatchpointEvent = null
        modificationWatchpointProfile
          .onModificationFieldWatchpoint("", "")
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        pipelineWithData.process(data)

        actual should be (expected)
      }

      it("should return a pipeline with the event data results filtered out for instance variables") {
        val expected = mock[ModificationWatchpointEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val pipelineWithData = Pipeline.newPipeline(
          classOf[ModificationWatchpointProfile#ModificationWatchpointEventAndData]
        )

        val modificationWatchpointProfile = new Object with ModificationWatchpointProfile {
          override def onModificationFieldWatchpointWithData(
            className: String,
            fieldName: String,
            extraArguments: JDIArgument*
          ): Pipeline[ModificationWatchpointEventAndData, ModificationWatchpointEventAndData] = ???

          override def onModificationInstanceWatchpointWithData(
            instanceVarName: String,
            extraArguments: JDIArgument*
          ): Pipeline[ModificationWatchpointEventAndData, ModificationWatchpointEventAndData] = {
            pipelineWithData
          }
        }

        var actual: ModificationWatchpointEvent = null
        modificationWatchpointProfile
          .onModificationInstanceWatchpoint("")
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
