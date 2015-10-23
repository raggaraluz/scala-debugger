package org.senkbeil.debugger.api.jdi.requests.filters

import org.senkbeil.debugger.api.jdi.requests.JDIRequestArgument

/**
 * Represents a filter for a JDI Request.
 */
trait JDIRequestFilter extends JDIRequestArgument {
  /**
   * Creates a new JDI request processor based on this filter.
   *
   * @return The new JDI request filter processor instance
   */
  def toProcessor: JDIRequestFilterProcessor
}
