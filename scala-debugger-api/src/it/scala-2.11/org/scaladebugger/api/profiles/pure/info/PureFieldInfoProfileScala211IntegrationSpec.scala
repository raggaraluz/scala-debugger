package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi.ThreadReference
import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{TestUtilities, VirtualMachineFixtures}

class PureFieldInfoProfileScala211IntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("PureFieldInfoProfile") {
    // $outer does not appear in this scenario for Scala 2.11
    ignore("should not expand $outer to its underlying fields") {
      val testClass = "org.scaladebugger.test.info.OuterScope"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      @volatile var t: Option[ThreadReference] = None
      val s = DummyScalaVirtualMachine.newInstance()

      // NOTE: Do not resume so we can check the variables at the stack frame
      s.withProfile(PureDebugProfile.Name)
        .getOrCreateBreakpointRequest(testFile, 17, NoResume)
        .foreach(e => t = Some(e.thread()))

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          val variableNames = s.withProfile(PureDebugProfile.Name)
            .tryThread(t.get)
            .flatMap(_.tryTopFrame)
            .map(_.allVariables)
            .map(_.map(_.name))
            .get

          // Should expand $outer field of closure into outer fields
          variableNames should contain theSameElementsAs Seq(
            "MODULE$",
            "x",
            "executionStart",
            "scala$App$$_Args",
            "scala$App$$initCode",
            "$outer",
            "newValue"
          )
        })
      }
    }
  }
}