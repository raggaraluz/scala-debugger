package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.Mirror

/**
 * Represents common methods between information-gathering profiles.
 */
trait CommonInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  def toJdiInstance: Mirror

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  def toPrettyString: String
}
