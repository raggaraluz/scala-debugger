package org.senkbeil.debugger.api.profiles.pure.vm

import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.{Event, VMDisconnectEvent}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import org.senkbeil.debugger.api.lowlevel.events.{JDIEventArgument, EventManager}
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.utils.LoopingTaskRunner
import org.senkbeil.debugger.api.lowlevel.events.EventType.VMDisconnectEventType
import test.JDIMockHelpers

class PureVMDisconnectProfileSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory with JDIMockHelpers
{
  // Workaround - see https://github.com/paulbutcher/ScalaMock/issues/33
  private class ZeroArgEventManager extends EventManager(
    stub[VirtualMachine],
    stub[LoopingTaskRunner],
    autoStart = false
  )
  private val mockEventManager = mock[ZeroArgEventManager]

  private val pureVMDisconnectProfile = new Object with PureVMDisconnectProfile {
    override protected val eventManager: EventManager = mockEventManager
  }

  describe("PureVMDisconnectProfile") {
    describe("#onVMDisconnectWithData") {
      it("should create a stream of events with data for disconnections") {
        val expected = (mock[VMDisconnectEvent], Seq(mock[JDIEventDataResult]))
        val arguments = Seq(mock[JDIEventArgument])

        (mockEventManager.addEventDataStream _).expects(
          VMDisconnectEventType, arguments
        ).returning(
          Pipeline.newPipeline(classOf[(Event, Seq[JDIEventDataResult])])
            .map(t => (expected._1, expected._2))
        ).once()

        var actual: (VMDisconnectEvent, Seq[JDIEventDataResult]) = null
        val pipeline =
          pureVMDisconnectProfile.onVMDisconnectWithData(arguments: _*)
        pipeline.foreach(actual = _)

        pipeline.process(expected)

        actual should be (expected)
      }
    }
  }
}
