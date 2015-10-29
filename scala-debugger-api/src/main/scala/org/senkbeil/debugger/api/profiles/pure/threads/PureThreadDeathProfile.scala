package org.senkbeil.debugger.api.profiles.pure.threads

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.profiles.traits.threads.ThreadDeathProfile

/**
 * Represents a pure profile for thread death events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureThreadDeathProfile extends ThreadDeathProfile {
  /**
   * Constructs a stream of thread death events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of thread death events and any retrieved data based on
   *         requests from extra arguments
   */
  override def onThreadDeathWithData(
    extraArguments: JDIArgument*
  ): Pipeline[ThreadDeathEventAndData, ThreadDeathEventAndData] = ???
}
