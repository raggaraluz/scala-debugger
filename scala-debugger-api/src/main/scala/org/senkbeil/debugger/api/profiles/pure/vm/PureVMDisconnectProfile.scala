package org.senkbeil.debugger.api.profiles.pure.vm

import com.sun.jdi.event.VMDisconnectEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.EventManager
import org.senkbeil.debugger.api.lowlevel.utils.JDIArgumentGroup
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline
import org.senkbeil.debugger.api.profiles.traits.vm.VMDisconnectProfile
import org.senkbeil.debugger.api.lowlevel.events.EventType.VMDisconnectEventType

/**
 * Represents a pure profile for vm disconnect events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureVMDisconnectProfile extends VMDisconnectProfile {
  protected val eventManager: EventManager

  /**
   * Constructs a stream of vm disconnect events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of vm disconnect events and any retrieved data based on
   *         requests from extra arguments
   */
  override def onVMDisconnectWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[VMDisconnectEventAndData] = {
    val JDIArgumentGroup(_, eArgs, _) = JDIArgumentGroup(extraArguments: _*)

    eventManager
      .addEventDataStream(VMDisconnectEventType, eArgs: _*)
      .map(t => (t._1.asInstanceOf[VMDisconnectEvent], t._2))
      .noop()
  }
}
