package org.senkbeil.debugger.api.virtualmachines

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Seconds, Span, Milliseconds}
import org.scalatest.{ParallelTestExecution, FunSpec, Matchers}
import test.{TestUtilities, VirtualMachineFixtures}

class ScalaVirtualMachineIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("ScalaVirtualMachine") {
    it("should indicate that it has started upon receiving the start event") {
      val testClass = "org.senkbeil.debugger.test.misc.MainUsingMethod"

      withVirtualMachine(testClass) { (s) =>
        eventually {
          assert(s.isStarted, "ScalaVirtualMachine not started!")
        }
      }
    }

    it("should return the class name of a Scala main method entrypoint") {
      val testClass = "org.senkbeil.debugger.test.misc.MainUsingMethod"

      withVirtualMachine(testClass) { (s) =>
        val expected = testClass

        // NOTE: This is not available until AFTER we have resumed from the
        //       start event (as the main method is not yet loaded)
        eventually {
          val actual = s.mainClassName
          actual should be(expected)
        }
      }
    }

    it("should return the class name of a Scala App entrypoint") {
      val testClass = "org.senkbeil.debugger.test.misc.MainUsingApp"

      withVirtualMachine(testClass) { (s) =>
        val expected = testClass

        // NOTE: This is not available until AFTER we have resumed from the
        //       start event (as the main method is not yet loaded)
        eventually {
          val actual = s.mainClassName
          actual should be(expected)
        }
      }
    }

    it("should return the arguments provided to the virtual machine") {
      val testClass = "org.senkbeil.debugger.test.misc.MainUsingApp"
      val testArguments = Seq("a", "b", "c")

      withVirtualMachine(testClass, testArguments) { (s) =>
        val expected = testArguments

        // NOTE: This is not available until AFTER we have resumed from the
        //       start event (as the main method is not yet loaded)
        eventually {
          val actual = s.commandLineArguments
          actual should contain theSameElementsInOrderAs expected
        }
      }
    }
  }
}
