package test

import com.senkbeil.debugger.LaunchingDebugger
import com.senkbeil.debugger.events.LoopingTaskRunner
import com.senkbeil.debugger.events.EventType._
import com.senkbeil.debugger.virtualmachines.ScalaVirtualMachine
import com.senkbeil.utils.LogLike
import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.ClassPrepareEvent

import scala.concurrent.future

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
      // Redirect output of JVM to our logs
      val process = virtualMachine.process()

      import scala.concurrent.ExecutionContext.Implicits.global
      future {
        val path = java.nio.file.Files.createTempFile("jvmfixture", ".out.log")
        logger.debug(s"Creating JVM Output File: ${path.toString}")

        java.nio.file.Files.copy(process.getInputStream, path,
          java.nio.file.StandardCopyOption.REPLACE_EXISTING)

        logger.debug(s"Deleting JVM Output File: ${path.toString}")
        java.nio.file.Files.delete(path)
      }
      future {
        val path = java.nio.file.Files.createTempFile("jvmfixture", ".err.log")
        logger.debug(s"Creating JVM Error File: ${path.toString}")

        java.nio.file.Files.copy(process.getErrorStream, path,
          java.nio.file.StandardCopyOption.REPLACE_EXISTING)

        logger.debug(s"Deleting JVM Error File: ${path.toString}")
        java.nio.file.Files.delete(path)
      }

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

        loopingTaskRunner.start()
        while (!virtualMachineReady) { Thread.sleep(1) }

        testCode(virtualMachine, scalaVirtualMachine)
      } finally {
        loopingTaskRunner.stop()
        launchingDebugger.stop()
      }
    }
  }
}
