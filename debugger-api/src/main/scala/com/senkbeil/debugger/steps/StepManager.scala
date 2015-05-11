package com.senkbeil.debugger.steps

import com.sun.jdi.{ThreadReference, VirtualMachine}
import com.sun.jdi.request.{EventRequest, StepRequest}

/**
 * Represents the manager for step requests.
 *
 * @param _virtualMachine The virtual machine whose step requests to
 *                        manage
 */
class StepManager(protected val _virtualMachine: VirtualMachine) {
  private val eventRequestManager = _virtualMachine.eventRequestManager()

  /**
   * Creates a new step request.
   *
   * @param threadReference The thread with which to perform the step
   * @param size The size of the step request (LINE/MIN)
   * @param depth The depth of the step request (INTO/OVER/OUT)
   * @param countFilter If greater than zero, adds a count filter to the
   *                    step request
   * @param enable If true, enables the created step request
   *
   * @return The newly-created step request
   */
  private def newStepRequest(
    threadReference: ThreadReference,
    size: Int,
    depth: Int,
    countFilter: Int = 1,
    enable: Boolean = true
  ): StepRequest = {
    val stepRequest =
      eventRequestManager.createStepRequest(threadReference, size, depth)

    if (countFilter > 0) stepRequest.addCountFilter(countFilter)
    if (enable) stepRequest.enable()

    stepRequest
  }

  /**
   * Creates a new step request to step into the next called method (in other
   * words, enter the next frame created by a function on the current line or
   * continue forward).
   *
   * @param threadReference The thread where the step will occur
   *
   * @return The newly-created step request
   */
  def stepInto(threadReference: ThreadReference): StepRequest = newStepRequest(
    threadReference,
    StepRequest.STEP_LINE,
    StepRequest.STEP_INTO
  )

  /**
   * Creates a new step request to step only into caller frames (in other
   * words, exit the current frame of execution).
   *
   * @param threadReference The thread where the step will occur
   *
   * @return The newly-created step request
   */
  def stepOut(threadReference: ThreadReference): StepRequest = newStepRequest(
    threadReference,
    StepRequest.STEP_LINE,
    StepRequest.STEP_OUT
  )

  /**
   * Creates a new step request to step only into caller frames or the current
   * frame (in other words, ignore any frames created by executing lines).
   *
   * @param threadReference The thread where the step will occur
   *
   * @return The newly-created step request
   */
  def stepOver(threadReference: ThreadReference): StepRequest = newStepRequest(
    threadReference,
    StepRequest.STEP_LINE,
    StepRequest.STEP_OVER
  )
}
