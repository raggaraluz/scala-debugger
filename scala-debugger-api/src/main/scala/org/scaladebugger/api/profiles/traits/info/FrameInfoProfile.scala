package org.scaladebugger.api.profiles.traits.info
//import acyclic.file

import com.sun.jdi.StackFrame

import scala.util.Try

/**
 * Represents the interface for frame-based interaction.
 */
trait FrameInfoProfile extends CommonInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: StackFrame

  /**
   * Retrieves the object representing 'this' in the current frame scope.
   *
   * @return Success containing the profile of this object, otherwise a failure
   */
  def tryGetThisObject: Try[ObjectInfoProfile] = Try(getThisObject)

  /**
   * Retrieves the object representing 'this' in the current frame scope.
   *
   * @return The profile of this object
   */
  def getThisObject: ObjectInfoProfile

  /**
   * Retrieves the thread associated with this frame.
   *
   * @return Success containing the profile of the thread, otherwise a failure
   */
  def tryGetCurrentThread: Try[ThreadInfoProfile] = Try(getCurrentThread)

  /**
   * Retrieves the thread associated with this frame.
   *
   * @return The profile of the thread
   */
  def getCurrentThread: ThreadInfoProfile

  /**
   * Retrieves the location associated with this frame.
   *
   * @return Success containing the profile of the location, otherwise a failure
   */
  def tryGetLocation: Try[LocationInfoProfile] = Try(getLocation)

  /**
   * Retrieves the location associated with this frame.
   *
   * @return The profile of the location
   */
  def getLocation: LocationInfoProfile

  /**
   * Retrieves the variable with the specified name from the frame.
   *
   * @param name The name of the variable to retrieve
   * @return Success containing profile of the variable if found, otherwise
   *         a failure
   */
  def tryGetVariable(name: String): Try[VariableInfoProfile] =
    Try(getVariable(name))

  /**
   * Retrieves the variable with the specified name from the frame.
   *
   * @param name The name of the variable to retrieve
   * @return Profile of the variable or throws an exception
   */
  def getVariable(name: String): VariableInfoProfile

  /**
   * Retrieves all variables in this frame.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryGetAllVariables: Try[Seq[VariableInfoProfile]] =
    Try(getAllVariables)

  /**
   * Retrieves all variables that represent arguments in this frame.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryGetArguments: Try[Seq[VariableInfoProfile]] = Try(getArguments)

  /**
   * Retrieves all variables that do not represent arguments in this frame.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryGetNonArguments: Try[Seq[VariableInfoProfile]] =
    Try(getNonArguments)

  /**
   * Retrieves all variables that represent local variables in this frame.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryGetLocalVariables: Try[Seq[VariableInfoProfile]] =
    Try(getLocalVariables)

  /**
   * Retrieves all variables that represent field variables in this frame.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryGetFieldVariables: Try[Seq[VariableInfoProfile]] =
    Try(getFieldVariables)

  /**
   * Retrieves all variables in this frame.
   *
   * @return The collection of variables as their profile equivalents
   */
  def getAllVariables: Seq[VariableInfoProfile]

  /**
   * Retrieves all variables that represent arguments in this frame.
   *
   * @return The collection of variables as their profile equivalents
   */
  def getArguments: Seq[VariableInfoProfile]

  /**
   * Retrieves all variables that do not represent arguments in this frame.
   *
   * @return The collection of variables as their profile equivalents
   */
  def getNonArguments: Seq[VariableInfoProfile]

  /**
   * Retrieves all variables that represent local variables in this frame.
   *
   * @return The collection of variables as their profile equivalents
   */
  def getLocalVariables: Seq[VariableInfoProfile]

  /**
   * Retrieves all variables that represent field variables in this frame.
   *
   * @return The collection of variables as their profile equivalents
   */
  def getFieldVariables: Seq[VariableInfoProfile]

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  override def toPrettyString: String = {
    val loc = this.tryGetLocation.map(_.toPrettyString).getOrElse("???")
    s"Frame at ($loc)"
  }
}
