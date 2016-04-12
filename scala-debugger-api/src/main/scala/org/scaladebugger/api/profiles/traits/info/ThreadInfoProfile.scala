package org.scaladebugger.api.profiles.traits.info
//import acyclic.file

import com.sun.jdi.ThreadReference

import scala.util.Try

/**
 * Represents the interface for thread-based interaction.
 */
trait ThreadInfoProfile extends ObjectInfoProfile with CommonInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ThreadReference

  /**
   * Represents the unique id of this thread.
   *
   * @return The unique id as a long
   */
  def uniqueId: Long

  /**
   * Represents the name of the thread.
   *
   * @return The thread name as a string
   */
  def name: String

  /**
   * Retrieves profiles for all frames in the stack.
   *
   * @return Success of collection of frame profiles, otherwise a failure
   */
  def tryGetFrames: Try[Seq[FrameInfoProfile]] = Try(getFrames)

  /**
   * Retrieves profiles for all frames in the stack.
   *
   * @return The collection of frame profiles
   */
  def getFrames: Seq[FrameInfoProfile]

  /**
   * Returns the total frames held in the current frame stack.
   *
   * @return Success containing the total number of frames, otherwise a failure
   */
  def tryGetTotalFrames: Try[Int] = Try(getTotalFrames)

  /**
   * Returns the total frames held in the current frame stack.
   *
   * @return The total number of frames
   */
  def getTotalFrames: Int

  /**
   * Retrieves the profile for the specified frame in the stack.
   *
   * @param index The index (starting with 0 being top) of the frame whose
   *              profile to retrieve
   * @return Success containing the new frame profile instance, otherwise
   *         a failure
   */
  def tryGetFrame(index: Int): Try[FrameInfoProfile] = Try(getFrame(index))

  /**
   * Retrieves the profile for the specified frame in the stack.
   *
   * @param index The index (starting with 0 being top) of the frame whose
   *              profile to retrieve
   * @return The new frame profile instance
   */
  def getFrame(index: Int): FrameInfoProfile

  /**
   * Retrieves the profile for the top (current) frame in the stack.
   *
   * @return Success containing the new frame profile instance, otherwise
   *         a failure
   */
  def tryGetTopFrame: Try[FrameInfoProfile] = tryGetFrame(0)

  /**
   * Retrieves the profile for the top (current) frame in the stack.
   *
   * @return The new frame profile instance
   */
  def getTopFrame: FrameInfoProfile = getFrame(0)

  /**
   * Retrieves an active variable within the thread's stack frames with the
   * matching name.
   *
   * @param name The name of the variable to find
   * @return Some variable if found, otherwise None
   */
  def findVariableByName(name: String): Option[VariableInfoProfile] = {
    // NOTE: Using for loop to reduce data retrieval (finding earlier is better)
    for (frameIndex <- 0 until this.getTotalFrames) {
      val frame = this.getFrame(frameIndex)
      val variable = frame.tryGetVariable(name).toOption
      if (variable.nonEmpty) return variable
    }

    // No variable found
    None
  }

  /**
   * Retrieves an active variable within the thread's stack frames with the
   * matching name.
   *
   * @param name The name of the variable to find
   * @return Success containing the variable if found, otherwise a failure
   */
  def tryFindVariableByName(name: String): Try[VariableInfoProfile] =
    Try(findVariableByName(name).get)

  /**
   * Retrieves an active variable from the specified stack frame using its
   * index and the offset of visible, local variables in the stack frame.
   *
   * @param frameIndex The index of the frame containing the variable
   * @param offsetIndex The offset within the frame to find the variable
   * @return Some variable if found, otherwise None
   */
  def findVariableByIndex(
    frameIndex: Int,
    offsetIndex: Int
  ): Option[VariableInfoProfile] = {
    this.getFrame(frameIndex)
      .getLocalVariables
      .find(_.offsetIndex == offsetIndex)
  }

  /**
   * Retrieves an active variable from the specified stack frame using its
   * index and the offset of visible, local variables in the stack frame.
   *
   * @param frameIndex The index of the frame containing the variable
   * @param offsetIndex The offset within the frame to find the variable
   * @return Some variable if found, otherwise None
   */
  def tryFindVariableByIndex(
    frameIndex: Int,
    offsetIndex: Int
  ): Try[VariableInfoProfile] =
    Try(findVariableByIndex(frameIndex, offsetIndex).get)

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  override def toPrettyString: String = {
    val threadName = this.name
    val uniqueHexCode = this.uniqueId.toHexString.toUpperCase()
    s"Thread $threadName (0x$uniqueHexCode)"
  }
}
