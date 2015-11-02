package org.senkbeil.debugger.api.profiles.traits.threads

import com.sun.jdi.event.ThreadStartEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline

/**
 * Represents the interface that needs to be implemented to provide
 * thread start functionality for a specific debug profile.
 */
trait ThreadStartProfile {
  /** Represents a thread start event and any associated data. */
  type ThreadStartEventAndData = (ThreadStartEvent, Seq[JDIEventDataResult])

  /**
   * Constructs a stream of thread start events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of thread start events
   */
  def onThreadStart(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ThreadStartEvent] = {
    onThreadStartWithData(extraArguments: _*).map(_._1).noop()
  }

  /**
   * Constructs a stream of thread start events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of thread start events and any retrieved data based on
   *         requests from extra arguments
   */
  def onThreadStartWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ThreadStartEventAndData]
}
