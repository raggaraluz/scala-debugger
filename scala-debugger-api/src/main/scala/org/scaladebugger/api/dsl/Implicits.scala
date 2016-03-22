package org.scaladebugger.api.dsl

import org.scaladebugger.api.dsl.breakpoints.BreakpointDSLWrapper
import org.scaladebugger.api.dsl.classes.{ClassUnloadDSLWrapper, ClassPrepareDSLWrapper}
import org.scaladebugger.api.dsl.events.EventDSLWrapper
import org.scaladebugger.api.dsl.exceptions.ExceptionDSLWrapper
import org.scaladebugger.api.dsl.info.{GrabInfoDSLWrapper, FrameInfoDSLWrapper}
import org.scaladebugger.api.dsl.methods.{MethodExitDSLWrapper, MethodEntryDSLWrapper}
import org.scaladebugger.api.dsl.monitors.{MonitorWaitDSLWrapper, MonitorWaitedDSLWrapper, MonitorContendedEnterDSLWrapper, MonitorContendedEnteredDSLWrapper}
import org.scaladebugger.api.dsl.steps.StepDSLWrapper
import org.scaladebugger.api.dsl.threads.{ThreadStartDSLWrapper, ThreadDeathDSLWrapper}
import org.scaladebugger.api.dsl.vm.{VMStartDSLWrapper, VMDisconnectDSLWrapper, VMDeathDSLWrapper}
import org.scaladebugger.api.dsl.watchpoints.{ModificationWatchpointDSLWrapper, AccessWatchpointDSLWrapper}
import org.scaladebugger.api.profiles.traits.breakpoints.BreakpointProfile
import org.scaladebugger.api.profiles.traits.classes.{ClassUnloadProfile, ClassPrepareProfile}
import org.scaladebugger.api.profiles.traits.events.EventProfile
import org.scaladebugger.api.profiles.traits.exceptions.ExceptionProfile
import org.scaladebugger.api.profiles.traits.info.{GrabInfoProfile, FrameInfoProfile}
import org.scaladebugger.api.profiles.traits.methods.{MethodExitProfile, MethodEntryProfile}
import org.scaladebugger.api.profiles.traits.monitors.{MonitorWaitProfile, MonitorWaitedProfile, MonitorContendedEnterProfile, MonitorContendedEnteredProfile}
import org.scaladebugger.api.profiles.traits.steps.StepProfile
import org.scaladebugger.api.profiles.traits.threads.{ThreadStartProfile, ThreadDeathProfile}
import org.scaladebugger.api.profiles.traits.vm.{VMStartProfile, VMDisconnectProfile, VMDeathProfile}
import org.scaladebugger.api.profiles.traits.watchpoints.{ModificationWatchpointProfile, AccessWatchpointProfile}

/**
 * Contains implicit classes to provide DSL-like methods to the debugger API.
 */
object Implicits {
  import scala.language.implicitConversions

  /** Converts breakpoint profile to implicit DSL wrapping. */
  implicit def BreakpointDSL(
    breakpointProfile: BreakpointProfile
  ): BreakpointDSLWrapper = new BreakpointDSLWrapper(breakpointProfile)

  /** Converts class prepare profile to implicit DSL wrapping. */
  implicit def ClassPrepareDSL(
    classPrepareProfile: ClassPrepareProfile
  ): ClassPrepareDSLWrapper = new ClassPrepareDSLWrapper(classPrepareProfile)

  /** Converts class unload profile to implicit DSL wrapping. */
  implicit def ClassUnloadDSL(
    classUnloadProfile: ClassUnloadProfile
  ): ClassUnloadDSLWrapper = new ClassUnloadDSLWrapper(classUnloadProfile)

  /** Converts event profile to implicit DSL wrapping. */
  implicit def EventDSL(
    eventProfile: EventProfile
  ): EventDSLWrapper = new EventDSLWrapper(eventProfile)

  /** Converts exception profile to implicit DSL wrapping. */
  implicit def ExceptionDSL(
    exceptionProfile: ExceptionProfile
  ): ExceptionDSLWrapper = new ExceptionDSLWrapper(exceptionProfile)

  /** Converts frame info profile to implicit DSL wrapping. */
  implicit def FrameInfoDSL(
    frameInfoProfile: FrameInfoProfile
  ): FrameInfoDSLWrapper = new FrameInfoDSLWrapper(frameInfoProfile)

  /** Converts grab info profile to implicit DSL wrapping. */
  implicit def GrabInfoDSL(
    grabInfoProfile: GrabInfoProfile
  ): GrabInfoDSLWrapper = new GrabInfoDSLWrapper(grabInfoProfile)

  /** Converts method entry profile to implicit DSL wrapping. */
  implicit def MethodEntryDSL(
    methodEntryProfile: MethodEntryProfile
  ): MethodEntryDSLWrapper = new MethodEntryDSLWrapper(methodEntryProfile)

  /** Converts method exit profile to implicit DSL wrapping. */
  implicit def MethodExitDSL(
    methodExitProfile: MethodExitProfile
  ): MethodExitDSLWrapper = new MethodExitDSLWrapper(methodExitProfile)

  /** Converts monitor contended entered profile to implicit DSL wrapping. */
  implicit def MonitorContendedEnteredDSL(
    monitorContendedEnteredProfile: MonitorContendedEnteredProfile
  ): MonitorContendedEnteredDSLWrapper = new MonitorContendedEnteredDSLWrapper(monitorContendedEnteredProfile)

  /** Converts monitor contended enter profile to implicit DSL wrapping. */
  implicit def MonitorContendedEnterDSL(
    monitorContendedEnterProfile: MonitorContendedEnterProfile
  ): MonitorContendedEnterDSLWrapper = new MonitorContendedEnterDSLWrapper(monitorContendedEnterProfile)

  /** Converts monitor waited profile to implicit DSL wrapping. */
  implicit def MonitorWaitedDSL(
    monitorWaitedProfile: MonitorWaitedProfile
  ): MonitorWaitedDSLWrapper = new MonitorWaitedDSLWrapper(monitorWaitedProfile)

  /** Converts monitor wait profile to implicit DSL wrapping. */
  implicit def MonitorWaitDSL(
    monitorWaitProfile: MonitorWaitProfile
  ): MonitorWaitDSLWrapper = new MonitorWaitDSLWrapper(monitorWaitProfile)

  /** Converts step profile to implicit DSL wrapping. */
  implicit def StepDSL(
    stepProfile: StepProfile
  ): StepDSLWrapper = new StepDSLWrapper(stepProfile)

  /** Converts thread death profile to implicit DSL wrapping. */
  implicit def ThreadDeathDSL(
    threadDeathProfile: ThreadDeathProfile
  ): ThreadDeathDSLWrapper = new ThreadDeathDSLWrapper(threadDeathProfile)

  /** Converts thread start profile to implicit DSL wrapping. */
  implicit def ThreadStartDSL(
    threadStartProfile: ThreadStartProfile
  ): ThreadStartDSLWrapper = new ThreadStartDSLWrapper(threadStartProfile)

  /** Converts vm death profile to implicit DSL wrapping. */
  implicit def VMDeathDSL(
    vmDeathProfile: VMDeathProfile
  ): VMDeathDSLWrapper = new VMDeathDSLWrapper(vmDeathProfile)

  /** Converts vm disconnect profile to implicit DSL wrapping. */
  implicit def VMDisconnectDSL(
    vmDisconnectProfile: VMDisconnectProfile
  ): VMDisconnectDSLWrapper = new VMDisconnectDSLWrapper(vmDisconnectProfile)

  /** Converts vm start profile to implicit DSL wrapping. */
  implicit def VMStartDSL(
    vmStartProfile: VMStartProfile
  ): VMStartDSLWrapper = new VMStartDSLWrapper(vmStartProfile)

  /** Converts access watchpoint profile to implicit DSL wrapping. */
  implicit def AccessWatchpointDSL(
    accessWatchpointProfile: AccessWatchpointProfile
  ): AccessWatchpointDSLWrapper = new AccessWatchpointDSLWrapper(accessWatchpointProfile)

  /** Converts modification watchpoint profile to implicit DSL wrapping. */
  implicit def ModificationWatchpointDSL(
    modificationWatchpointProfile: ModificationWatchpointProfile
  ): ModificationWatchpointDSLWrapper = new ModificationWatchpointDSLWrapper(modificationWatchpointProfile)
}
