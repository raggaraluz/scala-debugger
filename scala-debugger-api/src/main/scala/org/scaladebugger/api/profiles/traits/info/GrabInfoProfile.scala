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
   * Retrieves a thread profile for the given JDI thread reference.
   *
   * @param threadReference The JDI thread reference with which to wrap in
   *                        a thread info profile
   * @return Success containing the thread profile, otherwise a failure
   */
  def tryThread(threadReference: ThreadReference): Try[ThreadInfoProfile] =
    Try(thread(threadReference))

  /**
   * Retrieves a thread profile for the given JDI thread reference.
   *
   * @param threadReference The JDI thread reference with which to wrap in
   *                        a thread info profile
   * @return The new thread info profile
   */
  def thread(threadReference: ThreadReference): ThreadInfoProfile

  /**
   * Retrieves a thread profile for the thread reference whose unique id
   * matches the provided id.
   *
   * @param threadId The id of the thread
   * @return Success containing the thread profile if found, otherwise
   *         a failure
   */
  def tryThread(threadId: Long): Try[ThreadInfoProfile] =
    Try(thread(threadId))

  /**
   * Retrieves a thread profile for the thread reference whose unique id
   * matches the provided id.
   *
   * @param threadId The id of the thread
   * @return The profile of the matching thread, or throws an exception
   */
  def thread(threadId: Long): ThreadInfoProfile

  /**
   * Retrieves all classes contained in the remote JVM in the form of
   * reference type information.
   *
   * @return Success containing the collection of reference type info profiles,
   *         otherwise a failure
   */
  def tryClasses: Try[Seq[ReferenceTypeInfoProfile]] = Try(classes)

  /**
   * Retrieves all classes contained in the remote JVM in the form of
   * reference type information.
   *
   * @return The collection of reference type info profiles
   */
  def classes: Seq[ReferenceTypeInfoProfile]
}
