package org.scaladebugger.api.dsl.monitors

import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.profiles.traits.info.events.MonitorContendedEnterEventInfo
import org.scaladebugger.api.profiles.traits.requests.monitors.MonitorContendedEnterRequest
import org.scaladebugger.test.helpers.ParallelMockFunSpec

import scala.util.Success

class MonitorContendedEnterDSLWrapperSpec extends ParallelMockFunSpec
{
  private val mockMonitorContendedEnterProfile = mock[MonitorContendedEnterRequest]

  describe("MonitorContendedEnterDSLWrapper") {
    describe("#onMonitorContendedEnter") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.MonitorContendedEnterDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(classOf[MonitorContendedEnterEventInfo]))

        (mockMonitorContendedEnterProfile.tryGetOrCreateMonitorContendedEnterRequest _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockMonitorContendedEnterProfile.onMonitorContendedEnter(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeMonitorContendedEnter") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.MonitorContendedEnterDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(classOf[MonitorContendedEnterEventInfo])

        (mockMonitorContendedEnterProfile.getOrCreateMonitorContendedEnterRequest _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockMonitorContendedEnterProfile.onUnsafeMonitorContendedEnter(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onMonitorContendedEnterWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.MonitorContendedEnterDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Success(Pipeline.newPipeline(
          classOf[(MonitorContendedEnterEventInfo, Seq[JDIEventDataResult])]
        ))

        (mockMonitorContendedEnterProfile.tryGetOrCreateMonitorContendedEnterRequestWithData _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockMonitorContendedEnterProfile.onMonitorContendedEnterWithData(
          extraArguments: _*
        ) should be (returnValue)
      }
    }

    describe("#onUnsafeMonitorContendedEnterWithData") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.MonitorContendedEnterDSL

        val extraArguments = Seq(mock[JDIRequestArgument])
        val returnValue = Pipeline.newPipeline(
          classOf[(MonitorContendedEnterEventInfo, Seq[JDIEventDataResult])]
        )

        (mockMonitorContendedEnterProfile.getOrCreateMonitorContendedEnterRequestWithData _).expects(
          extraArguments
        ).returning(returnValue).once()

        mockMonitorContendedEnterProfile.onUnsafeMonitorContendedEnterWithData(
          extraArguments: _*
        ) should be (returnValue)
      }
    }
  }
}
