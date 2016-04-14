package org.scaladebugger.api.profiles.swappable.info
//import acyclic.file

import com.sun.jdi.ThreadReference
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.info.{ReferenceTypeInfoProfile, ThreadInfoProfile, GrabInfoProfile, MiscInfoProfile}

/**
 * Represents a swappable profile for grabbing various info that redirects the
 * invocation to another profile.
 */
trait SwappableGrabInfoProfile extends GrabInfoProfile {
  this: SwappableDebugProfileManagement =>
  override def getThread(
    threadReference: ThreadReference
  ): ThreadInfoProfile = withCurrentProfile.getThread(threadReference)

  override def getThread(
    threadId: Long
  ): ThreadInfoProfile = withCurrentProfile.getThread(threadId)

  override def getClasses: Seq[ReferenceTypeInfoProfile] =
    withCurrentProfile.getClasses
}
