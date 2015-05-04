package com.senkbeil.debugger.virtualmachines

import com.senkbeil.debugger.LaunchingDebugger
import com.senkbeil.debugger.events.LoopingTaskRunner
import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers, OneInstancePerTest}

class ScalaVirtualMachineIntegrationSpec extends FunSpec with Matchers
  with BeforeAndAfterAll
{
  private val testClass = classOf[com.senkbeil.test.java.Main]
  private val testArguments = Seq("a", "b", "c")
  private val launchingDebugger = new LaunchingDebugger(
    className = testClass.getName,
    commandLineArguments = testArguments,
    jvmOptions = Seq("-classpath", System.getProperty("java.class.path")),
    suspend = false // TODO: Investigate race condition resulting in failing
                    //       to get a listing of threads (too early) when true
  )
  private val loopingTaskRunner = new LoopingTaskRunner()
  private var scalaVirtualMachine: Option[ScalaVirtualMachine] = None

  override def beforeAll() = {
    launchingDebugger.start(
      (vm) => scalaVirtualMachine =
        Some(new ScalaVirtualMachine(vm, loopingTaskRunner)))
  }

  override def afterAll() = {
    launchingDebugger.stop()
  }

  describe("ScalaVirtualMachine") {
    describe("#mainClassName") {
      it("should return the class name entrypoint of the virtual machine") {
        val expected = testClass.getName

        val actual = scalaVirtualMachine.get.mainClassName

        actual should be (expected)
      }
    }

    describe("#commandLineArguments") {
      it("should return the arguments provided to the virtual machine") {
        val expected = testArguments

        val actual = scalaVirtualMachine.get.commandLineArguments

        actual should contain theSameElementsInOrderAs expected
      }
    }
  }
}
