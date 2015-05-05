package com.senkbeil.debugger

import com.sun.jdi.{VirtualMachine, VirtualMachineManager}
import com.sun.jdi.connect.{Connector, LaunchingConnector}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{OneInstancePerTest, Matchers, FunSpec}
import scala.collection.JavaConverters._

class LaunchingDebuggerSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{

  def createConnectorArgumentMock(
    setter: Boolean = false,
    getter: Option[String] = None
  ) = {
    val mockConnectorArgument = mock[Connector.Argument]
    if (getter.nonEmpty)
      (mockConnectorArgument.value _).expects().returning(getter.get).once()
    if (setter) (mockConnectorArgument.setValue _).expects(*).once()
    mockConnectorArgument
  }

  private val testClassName = "some class"
  private val testCommandLineArguments = Seq("a", "b", "c")
  private val testJvmOptions = Seq("d", "e", "f")
  private val testSuspend = false

  private implicit val mockVirtualMachineManager = mock[VirtualMachineManager]

  private class TestLaunchingDebugger(
    override val isAvailable: Boolean = true,
    private val shouldJdiLoad: Boolean = true
  ) extends LaunchingDebugger(
    className             = testClassName,
    commandLineArguments  = testCommandLineArguments,
    jvmOptions            = testJvmOptions,
    suspend               = testSuspend
  ) {
    override def assertJdiLoaded(): Unit =
      if (!shouldJdiLoad) throw new AssertionError
  }

  private val mockVirtualMachine = mock[VirtualMachine]

  describe("LaunchingDebugger") {
    describe("#start") {
      it("should throw an exception if unable to load JDI") {
        val launchingDebugger = new TestLaunchingDebugger(shouldJdiLoad = false)

        intercept[AssertionError] {
          launchingDebugger.start((_) => {})
        }
      }

      it("should throw an exception if already started") {
        val launchingDebugger = new TestLaunchingDebugger(shouldJdiLoad = true)

        // MOCK ===============================================================
        val mockLaunchingConnector = mock[LaunchingConnector]

        (mockLaunchingConnector.name _).expects()
          .returning("com.sun.jdi.CommandLineLaunch")

        (mockVirtualMachineManager.launchingConnectors _).expects()
          .returning(Seq(mockLaunchingConnector).asJava)

        (mockLaunchingConnector.defaultArguments _).expects().returning(Map(
          "main" -> createConnectorArgumentMock(setter = true),
          "options" -> createConnectorArgumentMock(
            setter = true, getter = Some("")
          ),
          "suspend" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockLaunchingConnector.launch _).expects(*).once()
        // MOCK ===============================================================

        launchingDebugger.start((_) => {})

        intercept[IllegalArgumentException] {
          launchingDebugger.start((_) => {})
        }
      }

      it("should throw an exception if unable to get the launching connector") {
        val launchingDebugger = new TestLaunchingDebugger(shouldJdiLoad = true)
        (mockVirtualMachineManager.launchingConnectors _).expects()
          .returning((Nil: Seq[LaunchingConnector]).asJava)

        intercept[AssertionError] {
          launchingDebugger.start((_) => {})
        }
      }

      it("should invoke the callback function with the new virtual machine") {
        val launchingDebugger = new TestLaunchingDebugger(shouldJdiLoad = true)

        // MOCK ===============================================================
        val mockLaunchingConnector = mock[LaunchingConnector]

        (mockLaunchingConnector.name _).expects()
          .returning("com.sun.jdi.CommandLineLaunch")

        (mockVirtualMachineManager.launchingConnectors _).expects()
          .returning(Seq(mockLaunchingConnector).asJava)

        (mockLaunchingConnector.defaultArguments _).expects().returning(Map(
          "main" -> createConnectorArgumentMock(setter = true),
          "options" -> createConnectorArgumentMock(
            setter = true, getter = Some("")
          ),
          "suspend" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockLaunchingConnector.launch _).expects(*)
          .returning(mockVirtualMachine).once()
        // MOCK ===============================================================

        val mockVirtualMachineFunc = mockFunction[VirtualMachine, Unit]

        mockVirtualMachineFunc.expects(mockVirtualMachine)

        launchingDebugger.start(mockVirtualMachineFunc)
      }
    }

    describe("#stop") {
      it("should throw an exception if not started") {
        val launchingDebugger = new TestLaunchingDebugger()

        intercept[IllegalArgumentException] {
          launchingDebugger.stop()
        }
      }

      it("should stop the running JVM process") {
        val launchingDebugger = new TestLaunchingDebugger(shouldJdiLoad = true)

        // MOCK ===============================================================
        val mockLaunchingConnector = mock[LaunchingConnector]

        (mockLaunchingConnector.name _).expects()
          .returning("com.sun.jdi.CommandLineLaunch")

        (mockVirtualMachineManager.launchingConnectors _).expects()
          .returning(Seq(mockLaunchingConnector).asJava)

        (mockLaunchingConnector.defaultArguments _).expects().returning(Map(
          "main" -> createConnectorArgumentMock(setter = true),
          "options" -> createConnectorArgumentMock(
            setter = true, getter = Some("")
          ),
          "suspend" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockLaunchingConnector.launch _).expects(*)
          .returning(mockVirtualMachine).once()
        // MOCK ===============================================================

        launchingDebugger.start((_) => {})

        val mockProcess = mock[Process]
        (mockProcess.destroy _).expects().once()
        (mockVirtualMachine.process _).expects().returning(mockProcess).once()

        launchingDebugger.stop()
      }
    }
  }
}
