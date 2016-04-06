package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi.ThreadReference
import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scalatest.concurrent.Eventually
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.{TestUtilities, VirtualMachineFixtures}

class PureReferenceTypeInfoProfileIntegrationSpec extends FunSpec with Matchers
  with ParallelTestExecution with VirtualMachineFixtures
  with TestUtilities with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(test.Constants.EventuallyTimeout),
    interval = scaled(test.Constants.EventuallyInterval)
  )

  describe("PureReferenceTypeInfoProfile") {
    it("should be able to retrieve a specific field from the class") {
      val testClass = "org.scaladebugger.test.info.Classes"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      withVirtualMachine(testClass) { (s) =>
        logTimeTaken(eventually {
          val ccc = s.withProfile(PureDebugProfile.Name).getClasses
          val fieldName = s.withProfile(PureDebugProfile.Name)
            .getClasses
            .find(_.getName == "org.scaladebugger.test.info.ExternalCaseClass")
            .get
            .getField("x").name

          fieldName should be ("x")
        })
      }
    }

    it("should be able to get a list of fields for the class") {
      val testClass = "org.scaladebugger.test.info.Classes"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      withVirtualMachine(testClass) { (s) =>
        logTimeTaken(eventually {
          val fieldNames = s.withProfile(PureDebugProfile.Name)
            .getClasses
            .find(_.getName == "org.scaladebugger.test.info.ExternalCaseClass")
            .get
            .getAllFields
            .map(_.name)

          fieldNames should contain theSameElementsAs Seq(
            "x", "y"
          )
        })
      }
    }

    it("should be able to retrieve specific methods by name from the class") {
      val testClass = "org.scaladebugger.test.info.Classes"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      withVirtualMachine(testClass) { (s) =>
        logTimeTaken(eventually {
          val methodNames = s.withProfile(PureDebugProfile.Name)
            .getClasses
            .find(_.getName == "org.scaladebugger.test.info.ExternalNormalClass")
            .get
            .getMethods("method1")
            .map(_.name)

          methodNames should be (Seq("method1"))
        })
      }
    }

    it("should be able to get a list of methods for the class") {
      val testClass = "org.scaladebugger.test.info.Classes"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      withVirtualMachine(testClass) { (s) =>
        logTimeTaken(eventually {
          val methodNames = s.withProfile(PureDebugProfile.Name)
            .getClasses
            .find(_.getName == "org.scaladebugger.test.info.ExternalNormalClass")
            .get
            .getAllMethods
            .map(_.name)

          methodNames should contain theSameElementsAs Seq(
            // Defined methods
            "method1",
            "method2",
            "method3",

            // Inherited methods
            "<clinit>",
            "<init>", // Overloaded method
            "<init>",
            "registerNatives",
            "getClass",
            "hashCode",
            "equals",
            "clone",
            "toString",
            "notify",
            "notifyAll",
            "wait", // Overloaded method
            "wait",
            "wait",
            "finalize"
          )
        })
      }
    }
  }
}
