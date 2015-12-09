package test

import java.io.{InputStreamReader, BufferedReader}
import java.util.concurrent.atomic.AtomicBoolean

import com.sun.jdi.event.VMStartEvent
import org.senkbeil.debugger.api.debuggers.LaunchingDebugger
import org.senkbeil.debugger.api.lowlevel.ManagerContainer
import org.senkbeil.debugger.api.lowlevel.events.EventType
import org.senkbeil.debugger.api.profiles.ProfileManager
import org.senkbeil.debugger.api.utils.{LoopingTaskRunner, Logging}
import org.senkbeil.debugger.api.virtualmachines.ScalaVirtualMachine
import EventType._
import com.sun.jdi.VirtualMachine

import scala.concurrent.Future
import scala.io.Source
import scala.util.Try

/**
 * Provides fixture methods to provide virtual machines running specified
 * files.
 */
trait VirtualMachineFixtures extends TestUtilities with Logging {
  /**
   * Creates a new virtual machine with the specified class and arguments.
   *
   * @param className The name of the main class to use as the JVM entrypoint
   * @param arguments The arguments to provide to the main class
   * @param testCode The test code to evaluate when the JVM connects, given the
   *                 ScalaVirtualMachine as an argument
   */
  def withVirtualMachine(
    className: String,
    arguments: Seq[String] = Nil
  )(
    testCode: (ScalaVirtualMachine) => Any
  ): Unit = withLazyVirtualMachine(className, arguments) { (s, start) =>
    start()
    testCode(s)
  }

  /**
   * Creates a new virtual machine with the specified class and arguments.
   *
   * @param className The name of the main class to use as the JVM entrypoint
   * @param arguments The arguments to provide to the main class
   * @param testCode The test code to evaluate when the JVM connects, given
   *                 the ScalaVirtualMachine and a function to execute when
   *                 ready to start the virtual machine
   */
  def withLazyVirtualMachine(
    className: String,
    arguments: Seq[String] = Nil
  )(
    testCode: (ScalaVirtualMachine, () => Unit) => Any
  ): Unit = {
    val launchingDebugger = LaunchingDebugger(
      className             = className,
      commandLineArguments  = arguments,
      jvmOptions            = Seq("-classpath", jvmClasspath),
      suspend               = true // This should always be true for our tests
    )

    launchingDebugger.start(startProcessingEvents = false, { s =>
      try {
        val startFunc = () => s.lowlevel.eventManager.start()

        testCode(s, startFunc)
      } finally {
        launchingDebugger.stop()
      }
    })
  }
}
