package test

import com.sun.jdi._
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.traits.info._

import scala.util.Try

/**
 * Contains test implementations of info classes. All methods throw an error.
 */
object InfoTestClasses {
  class NotOverriddenException extends Exception

  /** Exception thrown by all methods. */
  val DefaultException = new NotOverriddenException
  private def throwException() = throw DefaultException

  class TestThreadInfoProfile extends TestObjectInfoProfile with ThreadInfoProfile {
    override def getFrames: Seq[FrameInfoProfile] = throwException()
    override def name: String = throwException()
    override def getFrame(index: Int): FrameInfoProfile = throwException()
    override def getTotalFrames: Int = throwException()
    override def toJdiInstance: ThreadReference = throwException()
  }

  class TestValueInfoProfile extends ValueInfoProfile {
    override def typeName: String = throwException()
    override def isObject: Boolean = throwException()
    override def isPrimitive: Boolean = throwException()
    override def toObject: ObjectInfoProfile = throwException()
    override def toLocalValue: Any = throwException()
    override def toArray: ArrayInfoProfile = throwException()
    override def isString: Boolean = throwException()
    override def isArray: Boolean = throwException()
    override def isNull: Boolean = throwException()
    override def toJdiInstance: Value = throwException()
  }

  class TestVariableInfoProfile extends VariableInfoProfile {
    override def name: String = throwException()
    override def toValue: ValueInfoProfile = throwException()
    override def trySetValue(value: AnyVal): Try[AnyVal] = throwException()
    override def trySetValue(value: String): Try[String] = throwException()
    override def isArgument: Boolean = throwException()
    override def isLocal: Boolean = throwException()
    override def isField: Boolean = throwException()
    override def toJdiInstance: Mirror = throwException()
  }

  class TestObjectInfoProfile extends TestValueInfoProfile with ObjectInfoProfile {
    override def uniqueId: Long = throwException()
    override def invoke(methodInfoProfile: MethodInfoProfile, arguments: Seq[Any], jdiArguments: JDIArgument*): ValueInfoProfile = throwException()
    override def invoke(methodName: String, parameterTypeNames: Seq[String], arguments: Seq[Any], jdiArguments: JDIArgument*): ValueInfoProfile = throwException()
    override def getMethod(name: String, parameterTypeNames: String*): MethodInfoProfile = throwException()
    override def getFields: Seq[VariableInfoProfile] = throwException()
    override def getField(name: String): VariableInfoProfile = throwException()
    override def getMethods: Seq[MethodInfoProfile] = throwException()
    override def toJdiInstance: ObjectReference = throwException()
  }

  class TestMethodInfoProfile extends MethodInfoProfile {
    override def name: String = throwException()
    override def getReturnTypeName: String = throwException()
    override def getParameterTypeNames: Seq[String] = throwException()
    override def toJdiInstance: Method = throwException()
  }

  class TestGrabInfoProfile extends GrabInfoProfile {
    override def getThread(threadReference: ThreadReference): ThreadInfoProfile = throwException()
    override def getThread(threadId: Long): ThreadInfoProfile = throwException()
  }

  class TestFrameInfoProfile extends FrameInfoProfile {
    override def getThisObject: ObjectInfoProfile = throwException()
    override def getCurrentThread: ThreadInfoProfile = throwException()
    override def getFieldVariables: Seq[VariableInfoProfile] = throwException()
    override def getVariable(name: String): VariableInfoProfile = throwException()
    override def getAllVariables: Seq[VariableInfoProfile] = throwException()
    override def getLocalVariables: Seq[VariableInfoProfile] = throwException()
    override def getNonArguments: Seq[VariableInfoProfile] = throwException()
    override def getArguments: Seq[VariableInfoProfile] = throwException()
    override def toJdiInstance: StackFrame = throwException()
  }

  class TestArrayInfoProfile extends TestObjectInfoProfile with ArrayInfoProfile {
    override def length: Int = throwException()
    override def getValue(index: Int): ValueInfoProfile = throwException()
    override def setValues(index: Int, values: Seq[Any], srcIndex: Int, length: Int): Seq[Any] = throwException()
    override def setValues(values: Seq[Any]): Seq[Any] = throwException()
    override def getValues(index: Int, length: Int): Seq[ValueInfoProfile] = throwException()
    override def getValues: Seq[ValueInfoProfile] = throwException()
    override def setValue(index: Int, value: Any): Any = throwException()
    override def toJdiInstance: ArrayReference = throwException()
  }
}
