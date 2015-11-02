package org.senkbeil.debugger.api.profiles.traits.classes

import com.sun.jdi.event.ClassUnloadEvent
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline

class ClassUnloadProfileSpec extends FunSpec with Matchers with OneInstancePerTest
  with MockFactory
{
  describe("ClassUnloadProfile") {
    describe("#onClassUnload") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[ClassUnloadEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val pipelineWithData = Pipeline.newPipeline(
          classOf[ClassUnloadProfile#ClassUnloadEventAndData]
        )

        val classUnloadProfile = new Object with ClassUnloadProfile {
          override def onClassUnloadWithData(
            extraArguments: JDIArgument*
          ): IdentityPipeline[ClassUnloadEventAndData] = {
            pipelineWithData
          }
        }

        var actual: ClassUnloadEvent = null
        classUnloadProfile.onClassUnload().foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        pipelineWithData.process(data)

        actual should be (expected)
      }
    }
  }
}
