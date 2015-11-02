package org.senkbeil.debugger.api.profiles.pure.vm

import com.sun.jdi.event.VMStartEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.EventManager
import org.senkbeil.debugger.api.lowlevel.utils.JDIArgumentGroup
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.profiles.traits.vm.VMStartProfile
import org.senkbeil.debugger.api.lowlevel.events.EventType.VMStartEventType

/**
 * Represents a pure profile for vm start events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureVMStartProfile extends VMStartProfile {
  protected val eventManager: EventManager

  /**
   * Constructs a stream of vm start events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of vm start events and any retrieved data based on
   *         requests from extra arguments
   */
  override def onVMStartWithData(
    extraArguments: JDIArgument*
  ): Pipeline[VMStartEventAndData, VMStartEventAndData] = {
    val JDIArgumentGroup(_, eArgs, _) = JDIArgumentGroup(extraArguments: _*)

    eventManager
      .addEventDataStream(VMStartEventType, eArgs: _*)
      .map(t => (t._1.asInstanceOf[VMStartEvent], t._2))
      .noop()
  }
}
