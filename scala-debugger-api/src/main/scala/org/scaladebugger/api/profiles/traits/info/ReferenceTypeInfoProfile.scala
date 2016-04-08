package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.ReferenceType

import scala.util.Try

/**
 * Represents the interface for "reference type"-based interaction.
 */
trait ReferenceTypeInfoProfile extends CommonInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ReferenceType

  /**
   * Retrieves all fields declared in this type, its superclasses, implemented
   * interfaces, and superinterfaces.
   *
   * @return The collection of fields as variable info profiles
   */
  def getAllFields: Seq[VariableInfoProfile]

  /**
   * Retrieves all fields declared in this type, its superclasses, implemented
   * interfaces, and superinterfaces.
   *
   * @return Success containing the collection of fields as variable info
   *         profiles, otherwise a failure
   */
  def tryGetAllFields: Try[Seq[VariableInfoProfile]] = Try(getAllFields)

  /**
   * Retrieves unhidden and unambiguous fields in this type. Fields hidden
   * by other fields with the same name (in a more recently inherited class)
   * are not included. Fields that are ambiguously multiply inherited are also
   * not included. All other inherited fields are included.
   *
   * @return The collection of fields as variable info profiles
   */
  def getVisibleFields: Seq[VariableInfoProfile]

  /**
   * Retrieves unhidden and unambiguous fields in this type. Fields hidden
   * by other fields with the same name (in a more recently inherited class)
   * are not included. Fields that are ambiguously multiply inherited are also
   * not included. All other inherited fields are included.
   *
   * @return Success containing the collection of fields as variable info
   *         profiles, otherwise a failure
   */
  def tryGetVisibleFields: Try[Seq[VariableInfoProfile]] = Try(getVisibleFields)

  /**
   * Retrieves the visible field with the matching name.
   *
   * @param name The name of the field to retrieve
   * @return The field as a variable info profile
   */
  def getField(name: String): VariableInfoProfile

  /**
   * Retrieves the visible field with the matching name.
   *
   * @param name The name of the field to retrieve
   * @return Success containing the field as a variable info profile, otherwise
   *         a failure
   */
  def tryGetField(name: String): Try[VariableInfoProfile] = Try(getField(name))

  /**
   * Retrieves all methods declared in this type, its superclasses, implemented
   * interfaces, and superinterfaces.
   *
   * @return The collection of methods as method info profiles
   */
  def getAllMethods: Seq[MethodInfoProfile]

  /**
   * Retrieves all methods declared in this type, its superclasses, implemented
   * interfaces, and superinterfaces.
   *
   * @return Success containing the collection of methods as method info
   *         profiles, otherwise a failure
   */
  def tryGetAllMethods: Try[Seq[MethodInfoProfile]] = Try(getAllMethods)

  /**
   * Retrieves unhidden and unambiguous methods in this type. Methods hidden
   * by other methods with the same name (in a more recently inherited class)
   * are not included. Methods that are ambiguously multiply inherited are also
   * not included. All other inherited methods are included.
   *
   * @return The collection of methods as method info profiles
   */
  def getVisibleMethods: Seq[MethodInfoProfile]

  /**
   * Retrieves unhidden and unambiguous methods in this type. Methods hidden
   * by other methods with the same name (in a more recently inherited class)
   * are not included. Methods that are ambiguously multiply inherited are also
   * not included. All other inherited methods are included.
   *
   * @return Success containing the collection of methods as method info
   *         profiles, otherwise a failure
   */
  def tryGetVisibleMethods: Try[Seq[MethodInfoProfile]] = Try(getVisibleMethods)

  /**
   * Retrieves the visible methods with the matching name.
   *
   * @param name The name of the method to retrieve
   * @return The collection of method info profiles
   */
  def getMethods(name: String): Seq[MethodInfoProfile]

  /**
   * Retrieves the visible method with the matching name.
   *
   * @param name The name of the method to retrieve
   * @return Success containing the method as a method info profile, otherwise
   *         a failure
   */
  def tryGetMethods(name: String): Try[Seq[MethodInfoProfile]] =
    Try(getMethods(name))

  /**
   * Retrieves the classloader object which loaded the class associated with
   * this type.
   *
   * @return The profile representing the classloader
   */
  def getClassLoader: ClassLoaderInfoProfile

  /**
   * Retrieves the class object associated with this type.
   *
   * @return The profile representing the class
   */
  def getClassObject: ClassObjectInfoProfile

  /**
   * Retrieves the generic signature type if it exists.
   *
   * @return Some signature if it exists, otherwise None
   */
  def getGenericSignature: Option[String]

  /**
   * Retrieves reachable instances of this type.
   *
   * @param maxInstances The maximum number of instances to return, or zero
   *                     to get all reachable instances
   * @return The collection of object instances
   */
  def getInstances(maxInstances: Long): Seq[ObjectInfoProfile]

  /**
   * Retrieves reachable instances of this type.
   *
   * @param maxInstances The maximum number of instances to return, or zero
   *                     to get all reachable instances
   * @return Success containing the collection of object instances, otherwise
   *         a failure
   */
  def tryGetInstances(maxInstances: Long): Try[Seq[ObjectInfoProfile]] =
    Try(getInstances(maxInstances))

  /**
   * Retrieves all reachable instances of this type.
   *
   * @return The collection of object instances
   */
  def getAllInstances: Seq[ObjectInfoProfile] = getInstances(0)

  /**
   * Retrieves all reachable instances of this type.
   *
   * @return Success containing the collection of object instances, otherwise
   *         a failure
   */
  def tryGetAllInstances: Try[Seq[ObjectInfoProfile]] = Try(getAllInstances)

  /**
   * Indicates whether or not this type is abstract.
   *
   * @return True if abstract, otherwise false
   */
  def isAbstract: Boolean

  /**
   * Indicates whether or not this type is final.
   *
   * @return True if final, otherwise false
   */
  def isFinal: Boolean

  /**
   * Indicates whether or not this type has been initialized. This value is
   * the same as isPrepared for interfaces and is undefined for arrays and
   * primitive types.
   *
   * @return True if initialized, otherwise false
   */
  def isInitialized: Boolean

  /**
   * Indicates whether or not this type's class has been prepared.
   *
   * @return True if prepared, otherwise false
   */
  def isPrepared: Boolean

  /**
   * Indicates whether or not this type is static.
   *
   * @return True if static, otherwise false
   */
  def isStatic: Boolean

  /**
   * Indicates whether or not this type has been verified. This value is
   * the same as isPrepared for interfaces and is undefined for arrays and
   * primitive types.
   *
   * @return True if verified, otherwise false
   */
  def isVerified: Boolean

  /**
   * Retrieves and returns all valid locations for executable lines within
   * this type.
   *
   * @return The collection of location information
   */
  def getAllLineLocations: Seq[LocationInfoProfile]

  /**
   * Retrieves and returns all valid locations for executable lines within
   * this type.
   *
   * @return Success containing the collection of location information,
   *         otherwise a failure
   */
  def tryGetAllLineLocations: Try[Seq[LocationInfoProfile]] =
    Try(getAllLineLocations)

  /**
   * Retrieves and returns all valid locations for a specific executable line
   * within this type.
   *
   * @return The collection of location information
   */
  def getLocationsOfLine(line: Int): Seq[LocationInfoProfile]

  /**
   * Retrieves and returns all valid locations for a specific executable line
   * within this type.
   *
   * @return Success containing the collection of location information,
   *         otherwise a failure
   */
  def tryGetLocationsOfLine(line: Int): Try[Seq[LocationInfoProfile]] =
    Try(getLocationsOfLine(line))

  /**
   * Retrieves the major class version number defined in the class file format
   * of the JVM specification.
   *
   * @return The major version number
   */
  def getMajorVersion: Int

  /**
   * Retrieves the major class version number defined in the class file format
   * of the JVM specification.
   *
   * @return Success containing the major version number, otherwise a failure
   */
  def tryGetMajorVersion: Try[Int] = Try(getMajorVersion)

  /**
   * Retrieves the minor class version number defined in the class file format
   * of the JVM specification.
   *
   * @return The minor version number
   */
  def getMinorVersion: Int

  /**
   * Retrieves the minor class version number defined in the class file format
   * of the JVM specification.
   *
   * @return Success containing the minor version number, otherwise a failure
   */
  def tryGetMinorVersion: Try[Int] = Try(getMinorVersion)

  /**
   * Retrieves the fully-qualified class name of this type.
   *
   * @return The fully-qualified class name
   */
  def getName: String

  /**
   * Retrieves reference type information for all types declared inside this
   * tupe.
   *
   * @return The collection of reference type information
   */
  def getNestedTypes: Seq[ReferenceTypeInfoProfile]

  /**
   * Retrieves the source debug extension for this type.
   *
   * @return The source debug extension
   */
  def getSourceDebugExtension: String

  /**
   * Retrieves the source debug extension for this type.
   *
   * @return Success containing the source debug extension, otherwise a failure
   */
  def tryGetSourceDebugExtension: Try[String] = Try(getSourceDebugExtension)

  /**
   * Retrieves all identifying names for the source(s) corresponding to this
   * type.
   *
   * @return The collection of identifying names
   */
  def getSourceNames: Seq[String]

  /**
   * Retrieves all identifying names for the source(s) corresponding to this
   * type.
   *
   * @return Success containing the collection of identifying names, otherwise
   *         a failure
   */
  def tryGetSourceNames: Try[Seq[String]] = Try(getSourceNames)

  /**
   * Retrieves all source paths corresponding to this type.
   *
   * @return The collection of source paths
   */
  def getSourcePaths: Seq[String]

  /**
   * Retrieves all source paths corresponding to this type.
   *
   * @return Success containing the collection of source paths, otherwise a
   *         failure
   */
  def tryGetSourcePaths: Try[Seq[String]] = Try(getSourcePaths)

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  override def toPrettyString: String = {
    this.getName
  }
}
