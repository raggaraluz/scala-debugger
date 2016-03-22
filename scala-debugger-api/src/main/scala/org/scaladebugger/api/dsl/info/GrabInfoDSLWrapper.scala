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
  /** @see GrabInfoProfile#tryGetThread(ThreadReference) */
  def forThread(threadReference: ThreadReference): Try[ThreadInfoProfile] =
    grabInfoProfile.tryGetThread(threadReference)

  /** @see GrabInfoProfile#getThread(ThreadReference) */
  def forUnsafeThread(threadReference: ThreadReference): ThreadInfoProfile =
    grabInfoProfile.getThread(threadReference)

  /** @see GrabInfoProfile#tryGetThread(Long) */
  def forThread(threadId: Long): Try[ThreadInfoProfile] =
    grabInfoProfile.tryGetThread(threadId)

  /** @see GrabInfoProfile#getThread(Long) */
  def forUnsafeThread(threadId: Long): ThreadInfoProfile =
    grabInfoProfile.getThread(threadId)
}
