package org.scaladebugger.api.profiles.pure.info
//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._

import scala.util.{Failure, Try}
import scala.collection.JavaConverters._

/**
 * Represents a pure implementation of a stack frame profile that adds no custom
 * logic on top of the standard JDI.
 *
 * @param stackFrame The reference to the underlying JDI stack frame instance
 * @param index The index of the frame relative to the frame stack
 */
class PureFrameInfoProfile(
  private val stackFrame: StackFrame,
  val index: Int
) extends FrameInfoProfile {
  private lazy val threadReference = stackFrame.thread()
  private lazy val thisObjectProfile = newObjectProfile(stackFrame.thisObject())
  private lazy val currentThreadProfile = newThreadProfile(threadReference)
  private lazy val locationProfile = newLocationProfile(stackFrame.location())

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: StackFrame = stackFrame

  /**
   * Retrieves the object representing 'this' in the current frame scope.
   *
   * @return The profile of this object
   */
  override def getThisObject: ObjectInfoProfile = thisObjectProfile

  /**
   * Retrieves the thread associated with this frame.
   *
   * @return The profile of the thread
   */
  override def getCurrentThread: ThreadInfoProfile = currentThreadProfile

  /**
   * Retrieves the location associated with this frame.
   *
   * @return The profile of the location
   */
  override def getLocation: LocationInfoProfile = locationProfile

  /**
   * Retrieves the values of the arguments in this frame. As indicated by the
   * JDI spec, this can return values when no variable information is present.
   *
   * @return The collection of argument values in order as provided to the frame
   */
  override def getArgumentValues: Seq[ValueInfoProfile] = {
    import scala.collection.JavaConverters._
    stackFrame.getArgumentValues.asScala.map(newValueProfile)
  }

  /**
   * Retrieves the variable with the specified name from the frame.
   *
   * @param name The name of the variable to retrieve
   * @return Profile of the variable or throws an exception
   */
  override def getVariable(name: String): VariableInfoProfile = {
    // NOTE: Had to switch from name lookup in local variables to find method
    //       so we could include index information
    // NOTE: Ensime implementation looks for last match, so we will do the same
    getLocalVariables.reverseIterator.find(_.name == name)
      .getOrElse(thisObjectProfile.getField(name))
  }

  /**
   * Retrieves all variables that represent field variables in this frame.
   *
   * @return The collection of variables as their profile equivalents
   */
  override def getFieldVariables: Seq[VariableInfoProfile] = {
    thisObjectProfile.getFields
  }

  /**
   * Retrieves all variables in this frame.
   *
   * @return The collection of variables as their profile equivalents
   */
  override def getAllVariables: Seq[VariableInfoProfile] =
    getLocalVariables ++ getFieldVariables

  /**
   * Retrieves all variables that represent local variables in this frame.
   *
   * @return The collection of variables as their profile equivalents
   */
  override def getLocalVariables: Seq[IndexedVariableInfoProfile] = {
    stackFrame.visibleVariables().asScala.zipWithIndex.map { case (v, i) =>
      newLocalVariableProfile(v, i)
    }
  }

  /**
   * Retrieves all variables that do not represent arguments in this frame.
   *
   * @return The collection of variables as their profile equivalents
   */
  override def getNonArgumentLocalVariables: Seq[IndexedVariableInfoProfile] = {
    getLocalVariables.filterNot(_.isArgument)
  }

  /**
   * Retrieves all variables that represent arguments in this frame.
   *
   * @return The collection of variables as their profile equivalents
   */
  override def getArgumentLocalVariables: Seq[IndexedVariableInfoProfile] = {
    getLocalVariables.filter(_.isArgument)
  }

  protected def newLocalVariableProfile(
    localVariable: LocalVariable,
    offsetIndex: Int
  ): IndexedVariableInfoProfile = new PureLocalVariableInfoProfile(
    this,
    localVariable,
    offsetIndex
  )()

  protected def newObjectProfile(objectReference: ObjectReference): ObjectInfoProfile =
    new PureObjectInfoProfile(objectReference)(threadReference = threadReference)

  protected def newThreadProfile(threadReference: ThreadReference): ThreadInfoProfile =
    new PureThreadInfoProfile(threadReference)()

  protected def newLocationProfile(location: Location): LocationInfoProfile =
    new PureLocationInfoProfile(location)

  protected def newValueProfile(value: Value): ValueInfoProfile =
    new PureValueInfoProfile(value)
}
