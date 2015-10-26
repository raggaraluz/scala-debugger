package org.senkbeil.debugger.api.events.filters

import org.senkbeil.debugger.api.events.JDIEventArgument

/** Represents a filter for a JDI Event. */
trait JDIEventFilter extends JDIEventArgument {
  /**
   * Creates a new JDI event processor based on this filter.
   *
   * @return The new JDI event filter processor instance
   */
  override def toProcessor: JDIEventFilterProcessor
}
