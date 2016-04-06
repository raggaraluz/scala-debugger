package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.Location

import scala.util.Try

/**
 * Represents the interface for location-based interaction.
 */
trait LocationInfoProfile extends CommonInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: Location

  /**
   * Retrieves the reference type information for the type containing this
   * location.
   *
   * @return The reference type information
   */
  def getDeclaringType: ReferenceTypeInfoProfile

  /**
   * Retrieves the method information for the method containing this location.
   *
   * @return The method information
   */
  def getMethod: MethodInfoProfile

  /**
   * Retrieves the code position within the location's method.
   *
   * @return The code position, or -1 if not available
   */
  def getCodeIndex: Long

  /**
   * Retrieves the code position within the location's method.
   *
   * @return Some code position, or None if not available
   */
  def getCodeIndexOption: Option[Long] = Option(getCodeIndex).filter(_ >= 0)

  /**
   * Retrieves the line number associated with the location.
   *
   * @return The line number, or -1 if not available
   */
  def getLineNumber: Int

  /**
   * Retrieves the line number associated with the location.
   *
   * @return Some line number, or None if not available
   */
  def getLineNumberOption: Option[Int] = Option(getLineNumber).filter(_ >= 0)

  /**
   * Retrieves the identifying name for the source corresponding to this
   * location.
   *
   * @return The identifying name
   */
  def getSourceName: String

  /**
   * Retrieves the identifying name for the source corresponding to this
   * location.
   *
   * @return Success containing the identifying name, otherwise a failure
   */
  def tryGetSourceName: Try[String] = Try(getSourceName)

  /**
   * Retrieves the path to the source corresponding to this location.
   *
   * @return The source path
   */
  def getSourcePath: String

  /**
   * Retrieves the path to the source corresponding to this location.
   *
   * @return Success containing the source path, otherwise a failure
   */
  def tryGetSourcePath: Try[String] = Try(getSourcePath)
}
