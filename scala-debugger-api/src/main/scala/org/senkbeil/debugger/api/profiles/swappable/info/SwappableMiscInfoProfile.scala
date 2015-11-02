package org.senkbeil.debugger.api.profiles.swappable.info

import org.senkbeil.debugger.api.profiles.swappable.SwappableDebugProfile
import org.senkbeil.debugger.api.profiles.traits.info.MiscInfoProfile

/**
 * Represents a swappable profile for miscellaneous info that redirects the
 * invocation to another profile.
 */
trait SwappableMiscInfoProfile extends MiscInfoProfile {
  this: SwappableDebugProfile =>

  override def availableLinesForFile(fileName: String): Option[Seq[Int]] = {
    withCurrentProfile.availableLinesForFile(fileName)
  }

  override def commandLineArguments: Seq[String] = {
    withCurrentProfile.commandLineArguments
  }

  override def mainClassName: String = {
    withCurrentProfile.mainClassName
  }
}
