package com.senkbeil.debugger.events

import com.senkbeil.debugger.jdi.JDIHelperMethods
import com.senkbeil.utils.LogLike
import com.sun.jdi.VirtualMachine
import com.sun.jdi.event.Event

import scala.concurrent.{ExecutionContext, future}
import java.util.concurrent.ConcurrentHashMap

/**
 * Represents a manager for events coming in from a virtual machine.
 *
 * @param _virtualMachine The virtual machine whose events to manage
 * @param executionContext The context to use with events to schedule
 */
class EventManager(protected val _virtualMachine: VirtualMachine)
                  (implicit executionContext: ExecutionContext)
  extends JDIHelperMethods with LogLike
{
  type EventFunction = (Event) => Unit
  private val eventMap =
    new ConcurrentHashMap[Class[Event], Seq[EventFunction]]()

}
