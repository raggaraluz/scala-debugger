package test

import com.senkbeil.debugger.LaunchingDebugger
import com.senkbeil.debugger.events.LoopingTaskRunner
import com.senkbeil.debugger.events.EventType._
import com.senkbeil.debugger.virtualmachines.ScalaVirtualMachine
import com.senkbeil.utils.LogLike
import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.ClassPrepareEvent

/**
 * Provides fixture methods to provide virtual machines running specified
 * files.
 */
trait VirtualMachineFixtures extends LogLike {
  def withVirtualMachine(
    className: String,
    arguments: Seq[String] = Nil,
    suspend: Boolean = true,
    timeout: Long = 1000
  )(
    testCode: (VirtualMachine, ScalaVirtualMachine) => Any
  ) = {
    val launchingDebugger = new LaunchingDebugger(
      className = className,
      commandLineArguments = arguments,
      jvmOptions = Seq("-classpath", System.getProperty("java.class.path")),
      suspend = true // This should always be true for our tests, using resume
                     // above to indicate whether should resume after start
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
          logger.debug("Received start event for test, marking ready!")
          virtualMachineReady = true

          // Only resume the virtual machine IF we did not request it suspended
          logger.debug(s"Resuming vm: ${!suspend}")
          !suspend
        })

        // Wait for our specific entrypoint to be loaded
        var mainClassReady = false
        eventManager.addEventHandler(ClassPrepareEventType, e => {
          val classPrepareEvent = e.asInstanceOf[ClassPrepareEvent]
          val referenceType = classPrepareEvent.referenceType()
          val referenceTypeName = referenceType.name()
          logger.debug(s"Received new class: $referenceTypeName")

          if (referenceTypeName == className) {
            logger.debug(s"$className is now ready!")
            mainClassReady = true
          }

          !suspend
        })

        loopingTaskRunner.start()
        while (!virtualMachineReady || !mainClassReady) { Thread.sleep(1) }

        // NOTE: Need a slight delay for information to be ready (even after
        //       waiting for the start event)
        //Thread.sleep(100)
        testCode(virtualMachine, scalaVirtualMachine)
      } finally {
        loopingTaskRunner.stop()
        launchingDebugger.stop()
      }
    }
  }
}
