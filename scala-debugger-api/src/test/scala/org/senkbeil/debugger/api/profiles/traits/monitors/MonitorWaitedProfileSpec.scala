package org.senkbeil.debugger.api.profiles.traits.monitors

import com.sun.jdi.event.MonitorWaitedEvent
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline

import scala.util.{Failure, Success, Try}

class MonitorWaitedProfileSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val TestThrowable = new Throwable

  // Pipeline that is parent to the one that just streams the event
  private val TestPipelineWithData = Pipeline.newPipeline(
    classOf[MonitorWaitedProfile#MonitorWaitedEventAndData]
  )

  private val successMonitorWaitedProfile = new Object with MonitorWaitedProfile {
    override def onMonitorWaitedWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[MonitorWaitedEventAndData]] = {
      Success(TestPipelineWithData)
    }
  }

  private val failMonitorWaitedProfile = new Object with MonitorWaitedProfile {
    override def onMonitorWaitedWithData(
      extraArguments: JDIArgument*
    ): Try[IdentityPipeline[MonitorWaitedEventAndData]] = {
      Failure(TestThrowable)
    }
  }

  describe("MonitorWaitedProfile") {
    describe("#onMonitorWaited") {
      it("should return a pipeline with the event data results filtered out") {
        val expected = mock[MonitorWaitedEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: MonitorWaitedEvent = null
        successMonitorWaitedProfile
          .onMonitorWaited()
          .get
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should capture any exception as a failure") {
        val expected = TestThrowable

        var actual: Throwable = null
        failMonitorWaitedProfile
          .onMonitorWaited()
          .failed
          .foreach(actual = _)

        actual should be (expected)
      }
    }

    describe("#onUnsafeMonitorWaited") {
      it("should return a pipeline of events if successful") {
        val expected = mock[MonitorWaitedEvent]

        // Data to be run through pipeline
        val data = (expected, Seq(mock[JDIEventDataResult]))

        var actual: MonitorWaitedEvent = null
        successMonitorWaitedProfile
          .onUnsafeMonitorWaited()
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(data)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failMonitorWaitedProfile.onUnsafeMonitorWaited()
        }
      }
    }

    describe("#onUnsafeMonitorWaitedWithData") {
      it("should return a pipeline of events and data if successful") {
        // Data to be run through pipeline
        val expected = (mock[MonitorWaitedEvent], Seq(mock[JDIEventDataResult]))

        var actual: (MonitorWaitedEvent, Seq[JDIEventDataResult]) = null
        successMonitorWaitedProfile
          .onUnsafeMonitorWaitedWithData()
          .foreach(actual = _)

        // Funnel the data through the parent pipeline that contains data to
        // demonstrate that the pipeline with just the event is merely a
        // mapping on top of the pipeline containing the data
        TestPipelineWithData.process(expected)

        actual should be (expected)
      }

      it("should throw the exception if unsuccessful") {
        intercept[Throwable] {
          failMonitorWaitedProfile.onUnsafeMonitorWaitedWithData()
        }
      }
    }
  }
}

