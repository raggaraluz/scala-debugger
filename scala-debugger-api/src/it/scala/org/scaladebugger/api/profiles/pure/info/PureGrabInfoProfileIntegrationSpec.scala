package org.scaladebugger.api.profiles.pure.info
import acyclic.file

import com.sun.jdi.{ThreadReference, Value}
import com.sun.jdi.event.BreakpointEvent
import org.scaladebugger.api.lowlevel.events.EventType
import org.scaladebugger.api.lowlevel.events.EventType.BreakpointEventType
import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{TestUtilities, VirtualMachineFixtures}

import scala.util.Try

class PureGrabInfoProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("PureGrabInfoProfile") {
    it("should be able to find a variable by its name") {
      val testClass = "org.scaladebugger.test.info.Variables"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadReference] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 32, NoResume)
        .foreach(e => t = Some(e.thread()))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val thread = s.withProfile(PureDebugProfile.Name).getThread(t.get)

          // Should support retrieving local variables
          val localVariable = s.withProfile(PureDebugProfile.Name)
            .findVariableByName(thread, "a")
            .get
          localVariable.isLocal should be (true)
          localVariable.name should be ("a")

          // Should support retrieving fields
          val field = s.withProfile(PureDebugProfile.Name)
            .findVariableByName(thread, "z1")
            .get
          field.isField should be (true)
          field.name should be ("z1")
        })
      }
    }

    it("should be able to find a thread by its unique id") {
      val testClass = "org.scaladebugger.test.misc.LaunchingMain"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadReference] = None
      val s = DummyScalaVirtualMachine.newInstance()
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 7).foreach(e => t = Some(e.thread()))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val id = t.get.uniqueID()

          s.withProfile(PureDebugProfile.Name)
            .getThread(id).uniqueId should be (id)

          s.withProfile(PureDebugProfile.Name)
            .getThread(t.get).uniqueId should be (id)
        })
      }
    }
  }
}
