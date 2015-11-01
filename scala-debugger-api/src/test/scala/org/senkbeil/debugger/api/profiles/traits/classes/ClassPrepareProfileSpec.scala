package org.senkbeil.debugger.api.profiles.traits.classes

import com.sun.jdi.event.ClassPrepareEvent
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline

class ClassPrepareProfileSpec extends FunSpec with Matchers with OneInstancePerTest
  with MockFactory
{
  describe("ClassPrepareProfile") {
    describe("#onClassPrepare") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[ClassPrepareEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        // Pipeline that is parent to the one that just streams the event
        val pipelineWithData = Pipeline.newPipeline(
          classOf[ClassPrepareProfile#ClassPrepareEventAndData]
        )

        val classPrepareProfile = new Object with ClassPrepareProfile {
          override def onClassPrepareWithData(
            extraArguments: JDIArgument*
          ): Pipeline[ClassPrepareEventAndData, ClassPrepareEventAndData] = {
            pipelineWithData
          }
        }

        var actual: ClassPrepareEvent = null
        classPrepareProfile.onClassPrepare().foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        pipelineWithData.process(data)

        actual should be (expected)
      }
    }
  }
}
