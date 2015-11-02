package org.senkbeil.debugger.api.profiles.traits.breakpoints

import com.sun.jdi.event.BreakpointEvent
import org.senkbeil.debugger.api.lowlevel.JDIArgument
import org.senkbeil.debugger.api.lowlevel.events.data.JDIEventDataResult
import org.senkbeil.debugger.api.pipelines.Pipeline
import org.senkbeil.debugger.api.pipelines.Pipeline.IdentityPipeline

/**
 * Represents the interface that needs to be implemented to provide
 * breakpoint functionality for a specific debug profile.
 */
trait BreakpointProfile {
  /** Represents a breakpoint event and any associated data. */
  type BreakpointEventAndData = (BreakpointEvent, Seq[JDIEventDataResult])

  /**
   * Constructs a stream of breakpoint events for the specified file and line
   * number.
   *
   * @param fileName The name of the file where the breakpoint will be set
   * @param lineNumber The line number within the file where the breakpoint
   *                   will be set
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of breakpoint events
   */
  def onBreakpoint(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): IdentityPipeline[BreakpointEvent] = {
    onBreakpointWithData(
      fileName,
      lineNumber,
      extraArguments: _*
    ).map(_._1).noop()
  }

  /**
   * Constructs a stream of breakpoint events for the specified file and line
   * number.
   *
   * @param fileName The name of the file where the breakpoint will be set
   * @param lineNumber The line number within the file where the breakpoint
   *                   will be set
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of breakpoint events and any retrieved data based on
   *         requests from extra arguments
   */
  def onBreakpointWithData(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): IdentityPipeline[BreakpointEventAndData]
}
