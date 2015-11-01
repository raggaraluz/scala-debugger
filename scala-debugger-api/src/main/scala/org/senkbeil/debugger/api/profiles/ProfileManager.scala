package org.senkbeil.debugger.api.profiles

import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters._

import org.senkbeil.debugger.api.profiles.traits.DebugProfile

/**
 * Represents a manger for available debug profiles.
 */
class ProfileManager {
  /** Contains a mapping from profile name to instance. */
  private val profiles = new ConcurrentHashMap[String, DebugProfile]().asScala

  /**
   * Registers the profile using the provided name. Ignores any registration
   * under an already-used name.
   *
   * @param name The name of the profile to register
   * @param profile The profile to register
   */
  def register(name: String, profile: DebugProfile): Option[DebugProfile] = {
    require(name != null, "Profile name cannot be null!")
    require(name.nonEmpty, "Profile name cannot be empty!")
    require(profile != null, "Profile cannot be null!")

    val success = profiles.putIfAbsent(name, profile).isEmpty

    if (success) Some(profile)
    else None
  }

  /**
   * Unregisters the profile with the provided name.
   *
   * @param name The name of the profile to unregister
   *
   * @return Some debug profile if unregistered, otherwise None
   */
  def unregister(name: String): Option[DebugProfile] = {
    profiles.remove(name)
  }

  /**
   * Retrieves the profile with the provided name.
   *
   * @param name The name of the profile to retrieve
   *
   * @return Some debug profile if found, otherwise None
   */
  def retrieve(name: String): Option[DebugProfile] = {
    profiles.get(name)
  }
}
