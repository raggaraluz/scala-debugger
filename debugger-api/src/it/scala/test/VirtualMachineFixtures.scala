package test

import com.senkbeil.debugger.LaunchingDebugger
import com.senkbeil.debugger.events.LoopingTaskRunner
import com.senkbeil.debugger.events.EventType._
import com.senkbeil.debugger.virtualmachines.ScalaVirtualMachine
import com.sun.jdi.VirtualMachine

/**
 * Provides fixture methods to provide virtual machines running specified
 * files.
 */
trait VirtualMachineFixtures {
  def withVirtualMachine(
    className: String,
    arguments: Seq[String] = Nil
  )(
    testCode: (VirtualMachine, ScalaVirtualMachine) => Any
  ) = {
    val launchingDebugger = new LaunchingDebugger(
      className = className,
      commandLineArguments = arguments,
      jvmOptions = Seq("-classpath", System.getProperty("java.class.path")),
      suspend = true  // TODO: Investigate race condition resulting in failing
                      //       to get a listing of threads (too early) when true
    )
    val loopingTaskRunner = new LoopingTaskRunner()

    launchingDebugger.start { (virtualMachine) =>
      try {
        val scalaVirtualMachine = new ScalaVirtualMachine(
          virtualMachine,
          loopingTaskRunner
        )

        val eventManager = scalaVirtualMachine.eventManager

        // Wait for connection event to run the test code (ensures everything
        // is ready)
        var virtualMachineReady = false
        eventManager.addEventHandler(VMStartEventType, _ => {
          virtualMachineReady = true
        })

        loopingTaskRunner.start()
        while (!virtualMachineReady) { Thread.sleep(1) }

        // NOTE: Need a slight delay for information to be ready (even after
        //       waiting for the start event)
        Thread.sleep(100)
        testCode(virtualMachine, scalaVirtualMachine)
      } finally {
        loopingTaskRunner.stop()
        launchingDebugger.stop()
      }
    }
  }
}
