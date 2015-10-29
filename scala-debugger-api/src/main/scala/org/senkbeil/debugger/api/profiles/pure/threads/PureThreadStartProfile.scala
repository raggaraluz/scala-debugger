package org.senkbeil.debugger.api.profiles.pure.threads

import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.profiles.traits.threads.ThreadStartProfile

/**
 * Represents a pure profile for thread start events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureThreadStartProfile extends ThreadStartProfile {
  /**
   * Constructs a stream of thread start events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of thread start events and any retrieved data based on
   *         requests from extra arguments
   */
  override def onThreadStartWithData(
    extraArguments: JDIArgument*
  ): Pipeline[ThreadStartEventAndData, ThreadStartEventAndData] = ???
}
