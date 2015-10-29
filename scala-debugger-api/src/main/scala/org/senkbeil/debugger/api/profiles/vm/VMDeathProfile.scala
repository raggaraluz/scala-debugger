package org.senkbeil.debugger.api.profiles.vm

import com.sun.jdi.event.VMDeathEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.CloseablePipeline

/**
 * Represents the interface that needs to be implemented to provide
 * vm death functionality for a specific debug profile.
 */
trait VMDeathProfile {
  /** Represents a vm death event and any associated data. */
  type VMDeathEventAndData = (VMDeathEvent, Seq[JDIEventDataResult])

  /**
   * Constructs a stream of vm death events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of vm death events
   */
  def onVMDeath(
    extraArguments: JDIArgument*
  ): CloseablePipeline[VMDeathEvent, VMDeathEvent]

  /**
   * Constructs a stream of vm death events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of vm death events and any retrieved data based on
   *         requests from extra arguments
   */
  def onVMDeathWithData(
    extraArguments: JDIArgument*
  ): CloseablePipeline[VMDeathEventAndData, VMDeathEventAndData]
}
