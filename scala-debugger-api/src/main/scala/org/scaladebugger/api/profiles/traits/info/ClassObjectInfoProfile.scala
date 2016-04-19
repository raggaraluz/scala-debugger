package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.ClassObjectReference

/**
 * Represents the interface for "class object"-based interaction.
 */
trait ClassObjectInfoProfile extends ObjectInfoProfile with CommonInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ClassObjectReference

  /**
   * Retrieves the reference type information corresponding to this class
   * object.
   *
   * @return The reference type information
   */
  def reflectedType: ReferenceTypeInfoProfile
}
