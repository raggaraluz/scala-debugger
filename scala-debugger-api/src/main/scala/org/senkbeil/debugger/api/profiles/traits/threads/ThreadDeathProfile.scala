package org.senkbeil.debugger.api.profiles.traits.threads

import com.sun.jdi.event.ThreadDeathEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline

/**
 * Represents the interface that needs to be implemented to provide
 * thread death functionality for a specific debug profile.
 */
trait ThreadDeathProfile {
  /** Represents a thread death event and any associated data. */
  type ThreadDeathEventAndData = (ThreadDeathEvent, Seq[JDIEventDataResult])

  /**
   * Constructs a stream of thread death events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of thread death events
   */
  def onThreadDeath(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ThreadDeathEvent] = {
    onThreadDeathWithData(extraArguments: _*).map(_._1).noop()
  }

  /**
   * Constructs a stream of thread death events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of thread death events and any retrieved data based on
   *         requests from extra arguments
   */
  def onThreadDeathWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ThreadDeathEventAndData]
}
