package org.scaladebugger.api.profiles.traits.info
//import acyclic.file

import com.sun.jdi.Method

import scala.util.Try

/**
 * Represents the interface for method-based interaction.
 */
trait MethodInfoProfile extends CommonInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: Method

  /**
   * Returns the name of this method.
   *
   * @return The name of the method
   */
  def name: String

  /**
   * Returns the fully-qualified class names of the types for the parameters
   * of this method.
   *
   * @return Success containing the collection of parameter type names,
   *         otherwise a failure
   */
  def tryGetParameterTypeNames: Try[Seq[String]] = Try(getParameterTypeNames)

  /**
   * Returns the fully-qualified class names of the types for the parameters
   * of this method.
   *
   * @return The collection of parameter type names
   */
  def getParameterTypeNames: Seq[String]

  /**
   * Returns the fully-qualified class name of the type for the return value
   * of this method.
   *
   * @return Success containing the return type name, otherwise a failure
   */
  def tryGetReturnTypeName: Try[String] = Try(getReturnTypeName)

  /**
   * Returns the fully-qualified class name of the type for the return value
   * of this method.
   *
   * @return The return type name
   */
  def getReturnTypeName: String

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  override def toPrettyString: String = {
    val params = this.tryGetParameterTypeNames
      .map(_.mkString(","))
      .getOrElse("???")

    val returnType = this.tryGetReturnTypeName.getOrElse("???")

    s"def ${this.name}($params): $returnType"
  }
}
