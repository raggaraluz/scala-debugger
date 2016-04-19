package org.scaladebugger.api.dsl.info

import com.sun.jdi.ThreadReference
import org.scaladebugger.api.profiles.traits.info.{ThreadInfoProfile, GrabInfoProfile}

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param grabInfoProfile The profile to wrap
 */
class GrabInfoDSLWrapper private[dsl] (
  private val grabInfoProfile: GrabInfoProfile
) {
  /** @see GrabInfoProfile#tryThread(ThreadReference) */
  def forThread(threadReference: ThreadReference): Try[ThreadInfoProfile] =
    grabInfoProfile.tryThread(threadReference)

  /** @see GrabInfoProfile#thread(ThreadReference) */
  def forUnsafeThread(threadReference: ThreadReference): ThreadInfoProfile =
    grabInfoProfile.thread(threadReference)

  /** @see GrabInfoProfile#tryThread(Long) */
  def forThread(threadId: Long): Try[ThreadInfoProfile] =
    grabInfoProfile.tryThread(threadId)

  /** @see GrabInfoProfile#thread(Long) */
  def forUnsafeThread(threadId: Long): ThreadInfoProfile =
    grabInfoProfile.thread(threadId)
}
