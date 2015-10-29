package org.senkbeil.debugger.api.profiles

import org.senkbeil.debugger.api.profiles.breakpoints.BreakpointProfile
import org.senkbeil.debugger.api.profiles.classes.{ClassUnloadProfile, ClassPrepareProfile}
import org.senkbeil.debugger.api.profiles.events.EventProfile
import org.senkbeil.debugger.api.profiles.exceptions.ExceptionProfile
import org.senkbeil.debugger.api.profiles.methods.{MethodExitProfile, MethodEntryProfile}
import org.senkbeil.debugger.api.profiles.monitors.{MonitorWaitProfile, MonitorWaitedProfile, MonitorContendedEnterProfile, MonitorContendedEnteredProfile}
import org.senkbeil.debugger.api.profiles.steps.StepProfile
import org.senkbeil.debugger.api.profiles.threads.{ThreadStartProfile, ThreadDeathProfile}
import org.senkbeil.debugger.api.profiles.vm.VMDeathProfile
import org.senkbeil.debugger.api.profiles.watchpoints.{ModificationWatchpointProfile, AccessWatchpointProfile}

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
