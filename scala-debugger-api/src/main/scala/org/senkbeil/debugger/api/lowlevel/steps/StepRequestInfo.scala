package org.senkbeil.debugger.api.lowlevel.steps

import com.sun.jdi.ThreadReference
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents information about a step.
 *
 * @param threadReference The thread monitored for steps
 * @param size The size of the step (LINE/MIN)
 * @param depth The depth of the step (INTO/OVER/OUT)
 * @param extraArguments The additional arguments provided to the step request
 */
case class StepRequestInfo(
  threadReference: ThreadReference,
  size: Int,
  depth: Int,
  extraArguments: Seq[JDIRequestArgument] = Nil
)

