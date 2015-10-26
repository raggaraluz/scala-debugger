package org.senkbeil.debugger.api.requests.properties

import org.senkbeil.debugger.api.requests.JDIRequestArgument

/**
 * Represents a property for a JDI Request.
 */
trait JDIRequestProperty extends JDIRequestArgument {
  /**
   * Creates a new JDI request processor based on this property.
   *
   * @return The new JDI request property processor instance
   */
  def toProcessor: JDIRequestPropertyProcessor
}
