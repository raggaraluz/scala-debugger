package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{ClassLoaderInfoProfile, ClassObjectInfoProfile, _}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a reference type profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            reference type
 * @param referenceType The reference to the underlying JDI reference type
 */
class PureReferenceTypeInfoProfile(
  val scalaVirtualMachine: ScalaVirtualMachine,
  private val referenceType: ReferenceType
) extends ReferenceTypeInfoProfile {
  private lazy val defaultStratum: String = referenceType.defaultStratum()

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ReferenceType = referenceType

  /**
   * Retrieves the fully-qualified class name of this type.
   *
   * @return The fully-qualified class name
   */
  override def getName: String = referenceType.name()

  /**
   * Retrieves the generic signature type if it exists.
   *
   * @return Some signature if it exists, otherwise None
   */
  override def getGenericSignature: Option[String] =
    Option(referenceType.genericSignature())

  /**
   * Retrieves the source debug extension for this type.
   *
   * @return The source debug extension
   */
  override def getSourceDebugExtension: String =
    referenceType.sourceDebugExtension()

  /**
   * Retrieves all identifying names for the source(s) corresponding to this
   * type.
   *
   * @return The collection of identifying names
   */
  override def getSourceNames: Seq[String] = {
    import scala.collection.JavaConverters._
    referenceType.sourceNames(defaultStratum).asScala
  }

  /**
   * Retrieves all source paths corresponding to this type.
   *
   * @return The collection of source paths
   */
  override def getSourcePaths: Seq[String] = {
    import scala.collection.JavaConverters._
    referenceType.sourcePaths(defaultStratum).asScala
  }

  /**
   * Retrieves all fields declared in this type, its superclasses, implemented
   * interfaces, and superinterfaces.
   *
   * @return The collection of fields as variable info profiles
   */
  override def getAllFields: Seq[VariableInfoProfile] = {
    import scala.collection.JavaConverters._
    referenceType.allFields().asScala.map(newFieldProfile)
  }

  /**
   * Retrieves unhidden and unambiguous fields in this type. Fields hidden
   * by other fields with the same name (in a more recently inherited class)
   * are not included. Fields that are ambiguously multiply inherited are also
   * not included. All other inherited fields are included.
   *
   * @return The collection of fields as variable info profiles
   */
  override def getVisibleFields: Seq[VariableInfoProfile] = {
    import scala.collection.JavaConverters._
    referenceType.visibleFields().asScala.map(newFieldProfile)
  }

  /**
   * Retrieves the visible field with the matching name.
   *
   * @param name The name of the field to retrieve
   * @return The field as a variable info profile
   */
  override def getField(name: String): VariableInfoProfile = {
    newFieldProfile(Option(referenceType.fieldByName(name)).get)
  }

  /**
   * Retrieves all methods declared in this type, its superclasses, implemented
   * interfaces, and superinterfaces.
   *
   * @return The collection of methods as method info profiles
   */
  override def getAllMethods: Seq[MethodInfoProfile] = {
    import scala.collection.JavaConverters._
    referenceType.allMethods().asScala.map(newMethodProfile)
  }

  /**
   * Retrieves unhidden and unambiguous methods in this type. Methods hidden
   * by other methods with the same name (in a more recently inherited class)
   * are not included. Methods that are ambiguously multiply inherited are also
   * not included. All other inherited methods are included.
   *
   * @return The collection of methods as method info profiles
   */
  override def getVisibleMethods: Seq[MethodInfoProfile] = {
    import scala.collection.JavaConverters._
    referenceType.visibleMethods().asScala.map(newMethodProfile)
  }

  /**
   * Retrieves the visible methods with the matching name.
   *
   * @param name The name of the method to retrieve
   * @return The collection of method info profiles
   */
  override def getMethods(name: String): Seq[MethodInfoProfile] = {
    import scala.collection.JavaConverters._
    referenceType.methodsByName(name).asScala.map(newMethodProfile)
  }

  /**
   * Retrieves and returns all valid locations for a specific executable line
   * within this type.
   *
   * @return The collection of location information
   */
  override def getLocationsOfLine(line: Int): Seq[LocationInfoProfile] = {
    import scala.collection.JavaConverters._
    referenceType.locationsOfLine(line).asScala.map(newLocationProfile)
  }

  /**
   * Retrieves and returns all valid locations for executable lines within
   * this type.
   *
   * @return The collection of location information
   */
  override def getAllLineLocations: Seq[LocationInfoProfile] = {
    import scala.collection.JavaConverters._
    referenceType.allLineLocations().asScala.map(newLocationProfile)
  }

  /**
   * Retrieves reference type information for all types declared inside this
   * tupe.
   *
   * @return The collection of reference type information
   */
  override def getNestedTypes: Seq[ReferenceTypeInfoProfile] = {
    import scala.collection.JavaConverters._
    referenceType.nestedTypes().asScala.map(newReferenceTypeProfile)
  }

  /**
   * Retrieves reachable instances of this type.
   *
   * @param maxInstances The maximum number of instances to return, or zero
   *                     to get all reachable instances
   * @return The collection of object instances
   */
  override def getInstances(maxInstances: Long): Seq[ObjectInfoProfile] = {
    require(maxInstances >= 0, "Max instances cannot be negative!")

    import scala.collection.JavaConverters._
    referenceType.instances(maxInstances).asScala.map(newObjectProfile)
  }

  /**
   * Retrieves the classloader object which loaded the class associated with
   * this type.
   *
   * @return The profile representing the classloader
   */
  override def getClassLoader: ClassLoaderInfoProfile = newClassLoaderProfile(
    referenceType.classLoader()
  )

  /**
   * Retrieves the class object associated with this type.
   *
   * @return The profile representing the class
   */
  override def getClassObject: ClassObjectInfoProfile = newClassObjectProfile(
    referenceType.classObject()
  )

  /**
   * Indicates whether or not this type is final.
   *
   * @return True if final, otherwise false
   */
  override def isFinal: Boolean = referenceType.isFinal

  /**
   * Indicates whether or not this type's class has been prepared.
   *
   * @return True if prepared, otherwise false
   */
  override def isPrepared: Boolean = referenceType.isPrepared

  /**
   * Indicates whether or not this type has been initialized. This value is
   * the same as isPrepared for interfaces and is undefined for arrays and
   * primitive types.
   *
   * @return True if initialized, otherwise false
   */
  override def isInitialized: Boolean = referenceType.isInitialized

  /**
   * Indicates whether or not this type is static.
   *
   * @return True if static, otherwise false
   */
  override def isStatic: Boolean = referenceType.isStatic

  /**
   * Indicates whether or not this type is abstract.
   *
   * @return True if abstract, otherwise false
   */
  override def isAbstract: Boolean = referenceType.isAbstract

  /**
   * Indicates whether or not this type has been verified. This value is
   * the same as isPrepared for interfaces and is undefined for arrays and
   * primitive types.
   *
   * @return True if verified, otherwise false
   */
  override def isVerified: Boolean = referenceType.isVerified

  /**
   * Retrieves the major class version number defined in the class file format
   * of the JVM specification.
   *
   * @return The major version number
   */
  override def getMajorVersion: Int = referenceType.majorVersion()

  /**
   * Retrieves the minor class version number defined in the class file format
   * of the JVM specification.
   *
   * @return The minor version number
   */
  override def getMinorVersion: Int = referenceType.minorVersion()

  protected def newFieldProfile(field: Field): VariableInfoProfile =
    new PureFieldInfoProfile(scalaVirtualMachine, null, field)()

  protected def newMethodProfile(method: Method): MethodInfoProfile =
    new PureMethodInfoProfile(scalaVirtualMachine, method)

  protected def newObjectProfile(objectReference: ObjectReference): ObjectInfoProfile =
    new PureObjectInfoProfile(scalaVirtualMachine, objectReference)()

  protected def newLocationProfile(location: Location): LocationInfoProfile =
    new PureLocationInfoProfile(scalaVirtualMachine, location)

  protected def newClassObjectProfile(
    classObjectReference: ClassObjectReference
  ): ClassObjectInfoProfile = new PureClassObjectInfoProfile(
    scalaVirtualMachine,
    classObjectReference
  )(
    referenceType = referenceType
  )

  protected def newClassLoaderProfile(
    classLoaderReference: ClassLoaderReference
  ): ClassLoaderInfoProfile = new PureClassLoaderInfoProfile(
    scalaVirtualMachine,
    classLoaderReference
  )(
    referenceType = referenceType
  )

  protected def newReferenceTypeProfile(
    referenceType: ReferenceType
  ): ReferenceTypeInfoProfile = new PureReferenceTypeInfoProfile(
    scalaVirtualMachine,
    referenceType
  )
}
