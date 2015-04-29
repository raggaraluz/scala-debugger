package com.senkbeil.debugger

import com.senkbeil.debugger.jdi.JDILoader

/**
 * Represents the generic interface that all debugger instances implement.
 */
trait Debugger {
  protected val jdiLoader = new JDILoader(this.getClass.getClassLoader)

  /**
   * Determines whether or not the debugger is available for use.
   *
   * @return True if the debugger is available, otherwise false
   */
  def isAvailable: Boolean = jdiLoader.isJdiAvailable()

  /**
   * Starts the debugger, performing any necessary setup and ending with
   * an initialized debugger that is or will be capable of connecting to one or
   * more virtual machine instances.
   */
  def start(): Unit

  /**
   * Shuts down the debugger, releasing any connected virtual machines.
   */
  def stop(): Unit
}
