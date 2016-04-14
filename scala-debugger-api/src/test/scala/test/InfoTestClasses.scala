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

  class TestLocationInfoProfile extends LocationInfoProfile {
    override def toJdiInstance: Location = throwException()
    override def getSourcePath: String = throwException()
    override def getLineNumber: Int = throwException()
    override def getSourceName: String = throwException()
    override def getCodeIndex: Long = throwException()
    override def getDeclaringType: ReferenceTypeInfoProfile = throwException()
    override def getMethod: MethodInfoProfile = throwException()
  }

  class TestValueInfoProfile extends ValueInfoProfile {
    override def typeName: String = throwException()
    override def isObject: Boolean = throwException()
    override def isPrimitive: Boolean = throwException()
    override def toObject: ObjectInfoProfile = throwException()
    override def toPrimitive: PrimitiveInfoProfile = throwException()
    override def toLocalValue: Any = throwException()
    override def toArray: ArrayInfoProfile = throwException()
    override def isString: Boolean = throwException()
    override def isArray: Boolean = throwException()
    override def isVoid: Boolean = throwException()
    override def isNull: Boolean = throwException()
    override def toJdiInstance: Value = throwException()
  }

  class TestVariableInfoProfile extends VariableInfoProfile {
    override def name: String = throwException()
    override def toValue: ValueInfoProfile = throwException()
    override def setValue(value: AnyVal): AnyVal = throwException()
    override def setValue(value: String): String = throwException()
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
    override def getReferenceType: ReferenceTypeInfoProfile = throwException()
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
    override def getClasses: Seq[ReferenceTypeInfoProfile] = throwException()
  }

  class TestFrameInfoProfile extends FrameInfoProfile {
    override def index: Int = throwException()
    override def getThisObject: ObjectInfoProfile = throwException()
    override def getCurrentThread: ThreadInfoProfile = throwException()
    override def getLocation: LocationInfoProfile = throwException()
    override def getFieldVariables: Seq[VariableInfoProfile] = throwException()
    override def getVariable(name: String): VariableInfoProfile = throwException()
    override def getAllVariables: Seq[VariableInfoProfile] = throwException()
    override def getLocalVariables: Seq[IndexedVariableInfoProfile] = throwException()
    override def getArgumentValues: Seq[ValueInfoProfile] = throwException()
    override def getNonArgumentLocalVariables: Seq[IndexedVariableInfoProfile] = throwException()
    override def getArgumentLocalVariables: Seq[IndexedVariableInfoProfile] = throwException()
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

  class TestPrimitiveInfoProfile extends TestValueInfoProfile with PrimitiveInfoProfile {
    override def toLocalValue: AnyVal = throwException()
    override def isBoolean: Boolean = throwException()
    override def isFloat: Boolean = throwException()
    override def isDouble: Boolean = throwException()
    override def isInteger: Boolean = throwException()
    override def isLong: Boolean = throwException()
    override def isChar: Boolean = throwException()
    override def isByte: Boolean = throwException()
    override def isShort: Boolean = throwException()
    override def toJdiInstance: PrimitiveValue = throwException()
  }

  class TestReferenceTypeInfoProfile extends ReferenceTypeInfoProfile {
    override def toJdiInstance: ReferenceType = throwException()
    override def isFinal: Boolean = throwException()
    override def isPrepared: Boolean = throwException()
    override def getGenericSignature: Option[String] = throwException()
    override def getVisibleFields: Seq[VariableInfoProfile] = throwException()
    override def getName: String = throwException()
    override def getInstances(maxInstances: Long): Seq[ObjectInfoProfile] = throwException()
    override def isInitialized: Boolean = throwException()
    override def getAllFields: Seq[VariableInfoProfile] = throwException()
    override def getSourceNames: Seq[String] = throwException()
    override def getSourcePaths: Seq[String] = throwException()
    override def isStatic: Boolean = throwException()
    override def isAbstract: Boolean = throwException()
    override def getAllMethods: Seq[MethodInfoProfile] = throwException()
    override def getField(name: String): VariableInfoProfile = throwException()
    override def getClassLoader: ClassLoaderInfoProfile = throwException()
    override def isVerified: Boolean = throwException()
    override def getSourceDebugExtension: String = throwException()
    override def getMinorVersion: Int = throwException()
    override def getLocationsOfLine(line: Int): Seq[LocationInfoProfile] = throwException()
    override def getMethods(name: String): Seq[MethodInfoProfile] = throwException()
    override def getVisibleMethods: Seq[MethodInfoProfile] = throwException()
    override def getAllLineLocations: Seq[LocationInfoProfile] = throwException()
    override def getClassObject: ClassObjectInfoProfile = throwException()
    override def getMajorVersion: Int = throwException()
    override def getNestedTypes: Seq[ReferenceTypeInfoProfile] = throwException()
    override def tryGetAllFields: Try[Seq[VariableInfoProfile]] = throwException()
    override def tryGetVisibleFields: Try[Seq[VariableInfoProfile]] = throwException()
    override def tryGetField(name: String): Try[VariableInfoProfile] = throwException()
    override def tryGetAllMethods: Try[Seq[MethodInfoProfile]] = throwException()
    override def tryGetVisibleMethods: Try[Seq[MethodInfoProfile]] = throwException()
    override def tryGetMethods(name: String): Try[Seq[MethodInfoProfile]] = throwException()
    override def tryGetInstances(maxInstances: Long): Try[Seq[ObjectInfoProfile]] = throwException()
    override def getAllInstances: Seq[ObjectInfoProfile] = throwException()
    override def tryGetAllInstances: Try[Seq[ObjectInfoProfile]] = throwException()
    override def tryGetAllLineLocations: Try[Seq[LocationInfoProfile]] = throwException()
    override def tryGetLocationsOfLine(line: Int): Try[Seq[LocationInfoProfile]] = throwException()
    override def tryGetMajorVersion: Try[Int] = throwException()
    override def tryGetMinorVersion: Try[Int] = throwException()
    override def tryGetSourceDebugExtension: Try[String] = throwException()
    override def tryGetSourceNames: Try[Seq[String]] = throwException()
    override def tryGetSourcePaths: Try[Seq[String]] = throwException()
  }
}
