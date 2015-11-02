package org.senkbeil.debugger.api.profiles.traits.vm

import com.sun.jdi.event.VMStartEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline

/**
 * Represents the interface that needs to be implemented to provide
 * vm start functionality for a specific debug profile.
 */
trait VMStartProfile {
  /** Represents a vm death event and any associated data. */
  type VMStartEventAndData = (VMStartEvent, Seq[JDIEventDataResult])

  /**
   * Constructs a stream of vm start events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of vm start events
   */
  def onVMStart(
    extraArguments: JDIArgument*
  ): Pipeline[VMStartEvent, VMStartEvent] = {
    onVMStartWithData(extraArguments: _*).map(_._1).noop()
  }

  /**
   * Constructs a stream of vm start events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of vm start events and any retrieved data based on
   *         requests from extra arguments
   */
  def onVMStartWithData(
    extraArguments: JDIArgument*
  ): Pipeline[VMStartEventAndData, VMStartEventAndData]
}

