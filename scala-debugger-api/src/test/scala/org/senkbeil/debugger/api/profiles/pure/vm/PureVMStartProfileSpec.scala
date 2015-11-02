package org.senkbeil.debugger.api.profiles.pure.vm

import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.{Event, VMStartEvent}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.events.EventType.VMStartEventType
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.utils.LoopingTaskRunner
import test.JDIMockHelpers

class PureVMStartProfileSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory with JDIMockHelpers
{
  // Workaround - see https://github.com/paulbutcher/ScalaMock/issues/33
  private class ZeroArgEventManager extends EventManager(
    stub[VirtualMachine],
    stub[LoopingTaskRunner],
    autoStart = false
  )
  private val mockEventManager = mock[ZeroArgEventManager]

  private val pureVMStartProfile = new Object with PureVMStartProfile {
    override protected val eventManager: EventManager = mockEventManager
  }

  describe("PureVMStartProfile") {
    describe("#onVMStartWithData") {
      it("should create a stream of events with data for startions") {
        val expected = (mock[VMStartEvent], Seq(mock[JDIEventDataResult]))
        val arguments = Seq(mock[JDIEventArgument])

        (mockEventManager.addEventDataStream _).expects(
          VMStartEventType, arguments
        ).returning(
          Pipeline.newPipeline(classOf[(Event, Seq[JDIEventDataResult])])
            .map(t => (expected._1, expected._2))
        ).once()

        var actual: (VMStartEvent, Seq[JDIEventDataResult]) = null
        val pipeline =
          pureVMStartProfile.onVMStartWithData(arguments: _*)
        pipeline.foreach(actual = _)

        pipeline.process(expected)

        actual should be (expected)
      }
    }
  }
}
