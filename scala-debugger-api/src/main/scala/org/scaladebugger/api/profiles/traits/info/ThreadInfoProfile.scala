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
}
