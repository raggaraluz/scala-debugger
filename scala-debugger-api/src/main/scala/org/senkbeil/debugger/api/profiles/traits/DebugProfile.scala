package org.senkbeil.debugger.api.profiles.traits

import org.senkbeil.debugger.api.profiles.traits.breakpoints.BreakpointProfile
import org.senkbeil.debugger.api.profiles.traits.classes.{ClassPrepareProfile, ClassUnloadProfile}
import org.senkbeil.debugger.api.profiles.traits.events.EventProfile
import org.senkbeil.debugger.api.profiles.traits.exceptions.ExceptionProfile
import org.senkbeil.debugger.api.profiles.traits.methods.{MethodEntryProfile, MethodExitProfile}
import org.senkbeil.debugger.api.profiles.traits.monitors.{MonitorContendedEnterProfile, MonitorContendedEnteredProfile, MonitorWaitProfile, MonitorWaitedProfile}
import org.senkbeil.debugger.api.profiles.traits.steps.StepProfile
import org.senkbeil.debugger.api.profiles.traits.threads.{ThreadDeathProfile, ThreadStartProfile}
import org.senkbeil.debugger.api.profiles.traits.vm.VMDeathProfile
import org.senkbeil.debugger.api.profiles.traits.watchpoints.{AccessWatchpointProfile, ModificationWatchpointProfile}

/**
 * Represents the interface that needs to be implemented to provide
 * functionality for a specific debug profile.
 */
trait DebugProfile
  extends AccessWatchpointProfile
  with BreakpointProfile
  with ClassPrepareProfile
  with ClassUnloadProfile
  with EventProfile
  with ExceptionProfile
  with MethodEntryProfile
  with MethodExitProfile
  with ModificationWatchpointProfile
  with MonitorContendedEnteredProfile
  with MonitorContendedEnterProfile
  with MonitorWaitedProfile
  with MonitorWaitProfile
  with StepProfile
  with ThreadDeathProfile
  with ThreadStartProfile
  with VMDeathProfile
