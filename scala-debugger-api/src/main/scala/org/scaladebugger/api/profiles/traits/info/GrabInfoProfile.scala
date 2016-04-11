package org.scaladebugger.api.profiles.traits.info
//import acyclic.file

import com.sun.jdi.ThreadReference

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * the ability to grab various information for a specific debug profile.
 */
trait GrabInfoProfile {
  /**
   * Retrieves an active variable within the thread's stack frames with the
   * matching name.
   *
   * @param thread The thread (profile) whose stack frames to search through
   * @param name The name of the variable to find
   * @return Some variable if found, otherwise None
   */
  def findVariableByName(
    thread: ThreadInfoProfile,
    name: String
  ): Option[VariableInfoProfile] = {
    // NOTE: Using for loop to reduce data retrieval (finding earlier is better)
    for (frameIndex <- 0 until thread.getTotalFrames) {
      val frame = thread.getFrame(frameIndex)
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
   * @param thread The thread (profile) whose stack frames to search through
   * @param name The name of the variable to find
   * @return Success containing the variable if found, otherwise a failure
   */
  def tryFindVariableByName(
    thread: ThreadInfoProfile,
    name: String
  ): Try[VariableInfoProfile] = Try(findVariableByName(thread, name).get)

  /**
   * Retrieves a thread profile for the given JDI thread reference.
   *
   * @param threadReference The JDI thread reference with which to wrap in
   *                        a thread info profile
   * @return Success containing the thread profile, otherwise a failure
   */
  def tryGetThread(threadReference: ThreadReference): Try[ThreadInfoProfile] =
    Try(getThread(threadReference))

  /**
   * Retrieves a thread profile for the given JDI thread reference.
   *
   * @param threadReference The JDI thread reference with which to wrap in
   *                        a thread info profile
   * @return The new thread info profile
   */
  def getThread(threadReference: ThreadReference): ThreadInfoProfile

  /**
   * Retrieves a thread profile for the thread reference whose unique id
   * matches the provided id.
   *
   * @param threadId The id of the thread
   * @return Success containing the thread profile if found, otherwise
   *         a failure
   */
  def tryGetThread(threadId: Long): Try[ThreadInfoProfile] =
    Try(getThread(threadId))

  /**
   * Retrieves a thread profile for the thread reference whose unique id
   * matches the provided id.
   *
   * @param threadId The id of the thread
   * @return The profile of the matching thread, or throws an exception
   */
  def getThread(threadId: Long): ThreadInfoProfile

  /**
   * Retrieves all classes contained in the remote JVM in the form of
   * reference type information.
   *
   * @return Success containing the collection of reference type info profiles,
   *         otherwise a failure
   */
  def tryGetClasses: Try[Seq[ReferenceTypeInfoProfile]] = Try(getClasses)

  /**
   * Retrieves all classes contained in the remote JVM in the form of
   * reference type information.
   *
   * @return The collection of reference type info profiles
   */
  def getClasses: Seq[ReferenceTypeInfoProfile]
}
