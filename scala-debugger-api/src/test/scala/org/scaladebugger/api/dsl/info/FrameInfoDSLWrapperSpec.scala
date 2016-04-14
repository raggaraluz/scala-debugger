package org.scaladebugger.api.dsl.info

import com.sun.jdi.StackFrame
import org.scaladebugger.api.profiles.traits.info._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

import scala.util.{Success, Try}

class FrameInfoDSLWrapperSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  // NOTE: Cannot mock Function0 with no parentheses
  //private val mockFrameInfoProfile = mock[FrameInfoProfile]
  
  private val testFrameInfoProfile = new TestFrameInfoProfile

  describe("FrameInfoDSLWrapper") {
    describe("#withThisObject") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Success(mock[ObjectInfoProfile])

        mockTryGetThisObjectFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.withThisObject should be (returnValue)
      }
    }

    describe("#withUnsafeThisObject") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = mock[ObjectInfoProfile]

        mockGetThisObjectFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.withUnsafeThisObject should be (returnValue)
      }
    }

    describe("#withCurrentThread") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Success(mock[ThreadInfoProfile])

        mockTryGetCurrentThreadFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.withCurrentThread should be (returnValue)
      }
    }

    describe("#withUnsafeCurrentThread") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = mock[ThreadInfoProfile]

        mockGetCurrentThreadFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.withUnsafeCurrentThread should be (returnValue)
      }
    }

    describe("#forVariable") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val name = "someName"
        val returnValue = Success(mock[VariableInfoProfile])

        mockTryGetVariableFunction.expects(name)
          .returning(returnValue).once()

        testFrameInfoProfile.forVariable(name) should be (returnValue)
      }
    }

    describe("#forUnsafeVariable") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val name = "someName"
        val returnValue = mock[VariableInfoProfile]

        mockGetVariableFunction.expects(name)
          .returning(returnValue).once()

        testFrameInfoProfile.forUnsafeVariable(name) should be (returnValue)
      }
    }

    describe("#forAllVariables") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Success(Seq(mock[VariableInfoProfile]))

        mockTryGetAllVariablesFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forAllVariables should be (returnValue)
      }
    }

    describe("#forUnsafeAllVariables") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Seq(mock[VariableInfoProfile])

        mockGetAllVariablesFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forUnsafeAllVariables should be (returnValue)
      }
    }

    describe("#forArguments") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Success(Seq(mock[IndexedVariableInfoProfile]))

        mockTryGetArgumentsFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forArguments should be (returnValue)
      }
    }

    describe("#forUnsafeArguments") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Seq(mock[IndexedVariableInfoProfile])

        mockGetArgumentsFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forUnsafeArguments should be (returnValue)
      }
    }

    describe("#forNonArguments") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Success(Seq(mock[IndexedVariableInfoProfile]))

        mockTryGetNonArgumentsFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forNonArguments should be (returnValue)
      }
    }

    describe("#forUnsafeNonArguments") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Seq(mock[IndexedVariableInfoProfile])

        mockGetNonArgumentsFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forUnsafeNonArguments should be (returnValue)
      }
    }

    describe("#forLocalVariables") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Success(Seq(mock[IndexedVariableInfoProfile]))

        mockTryGetLocalVariablesFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forLocalVariables should be (returnValue)
      }
    }

    describe("#forUnsafeLocalVariables") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Seq(mock[IndexedVariableInfoProfile])

        mockGetLocalVariablesFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forUnsafeLocalVariables should be (returnValue)
      }
    }

    describe("#forFieldVariables") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Success(Seq(mock[VariableInfoProfile]))

        mockTryGetFieldVariablesFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forFieldVariables should be (returnValue)
      }
    }

    describe("#forUnsafeFieldVariables") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Seq(mock[VariableInfoProfile])

        mockGetFieldVariablesFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forUnsafeFieldVariables should be (returnValue)
      }
    }
  }

  private val mockIndexFunction = mockFunction[Int]
  private val mockTryGetThisObjectFunction = mockFunction[Try[ObjectInfoProfile]]
  private val mockGetThisObjectFunction = mockFunction[ObjectInfoProfile]
  private val mockTryGetCurrentThreadFunction = mockFunction[Try[ThreadInfoProfile]]
  private val mockGetCurrentThreadFunction = mockFunction[ThreadInfoProfile]
  private val mockTryGetLocationFunction = mockFunction[Try[LocationInfoProfile]]
  private val mockGetLocationFunction = mockFunction[LocationInfoProfile]
  private val mockTryGetArgumentValuesFunction = mockFunction[Try[Seq[ValueInfoProfile]]]
  private val mockGetArgumentValuesFunction = mockFunction[Seq[ValueInfoProfile]]
  private val mockTryGetVariableFunction = mockFunction[String, Try[VariableInfoProfile]]
  private val mockGetVariableFunction = mockFunction[String, VariableInfoProfile]
  private val mockTryGetAllVariablesFunction = mockFunction[Try[Seq[VariableInfoProfile]]]
  private val mockTryGetArgumentsFunction = mockFunction[Try[Seq[IndexedVariableInfoProfile]]]
  private val mockTryGetNonArgumentsFunction = mockFunction[Try[Seq[IndexedVariableInfoProfile]]]
  private val mockTryGetLocalVariablesFunction = mockFunction[Try[Seq[IndexedVariableInfoProfile]]]
  private val mockTryGetFieldVariablesFunction = mockFunction[Try[Seq[VariableInfoProfile]]]
  private val mockGetAllVariablesFunction = mockFunction[Seq[VariableInfoProfile]]
  private val mockGetArgumentsFunction = mockFunction[Seq[IndexedVariableInfoProfile]]
  private val mockGetNonArgumentsFunction = mockFunction[Seq[IndexedVariableInfoProfile]]
  private val mockGetLocalVariablesFunction = mockFunction[Seq[IndexedVariableInfoProfile]]
  private val mockGetFieldVariablesFunction = mockFunction[Seq[VariableInfoProfile]]
  private val mockToJdiInstanceFunction = mockFunction[StackFrame]

  private class TestFrameInfoProfile extends FrameInfoProfile {
    override def index: Int = mockIndexFunction()
    override def tryGetThisObject: Try[ObjectInfoProfile] = mockTryGetThisObjectFunction()
    override def getThisObject: ObjectInfoProfile = mockGetThisObjectFunction()
    override def tryGetCurrentThread: Try[ThreadInfoProfile] = mockTryGetCurrentThreadFunction()
    override def getCurrentThread: ThreadInfoProfile = mockGetCurrentThreadFunction()
    override def tryGetLocation: Try[LocationInfoProfile] = mockTryGetLocationFunction()
    override def getLocation: LocationInfoProfile = mockGetLocationFunction()
    override def tryGetArgumentValues: Try[Seq[ValueInfoProfile]] = mockTryGetArgumentValuesFunction()
    override def getArgumentValues: Seq[ValueInfoProfile] = mockGetArgumentValuesFunction()
    override def tryGetVariable(name: String): Try[VariableInfoProfile] = mockTryGetVariableFunction(name)
    override def getVariable(name: String): VariableInfoProfile = mockGetVariableFunction(name)
    override def tryGetAllVariables: Try[Seq[VariableInfoProfile]] = mockTryGetAllVariablesFunction()
    override def tryGetArgumentLocalVariables: Try[Seq[IndexedVariableInfoProfile]] = mockTryGetArgumentsFunction()
    override def tryGetNonArgumentLocalVariables: Try[Seq[IndexedVariableInfoProfile]] = mockTryGetNonArgumentsFunction()
    override def tryGetLocalVariables: Try[Seq[IndexedVariableInfoProfile]] = mockTryGetLocalVariablesFunction()
    override def tryGetFieldVariables: Try[Seq[VariableInfoProfile]] = mockTryGetFieldVariablesFunction()
    override def getAllVariables: Seq[VariableInfoProfile] = mockGetAllVariablesFunction()
    override def getArgumentLocalVariables: Seq[IndexedVariableInfoProfile] = mockGetArgumentsFunction()
    override def getNonArgumentLocalVariables: Seq[IndexedVariableInfoProfile] = mockGetNonArgumentsFunction()
    override def getLocalVariables: Seq[IndexedVariableInfoProfile] = mockGetLocalVariablesFunction()
    override def getFieldVariables: Seq[VariableInfoProfile] = mockGetFieldVariablesFunction()
    override def toJdiInstance: StackFrame = mockToJdiInstanceFunction()
  }
}
