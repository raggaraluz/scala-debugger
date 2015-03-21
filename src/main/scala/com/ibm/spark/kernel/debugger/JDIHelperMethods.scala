package com.ibm.spark.kernel.debugger

import com.ibm.spark.kernel.utils.LogLike
import com.sun.jdi.{Location, ReferenceType, VirtualMachine}
import collection.JavaConverters._

import scala.util.Try

trait JDIHelperMethods extends LogLike {
  protected val _virtualMachine: VirtualMachine

  /**
   * Executes the provided block of code during the state of a suspended
   * virtual machine. This method is synchronized to prevent another thread
   * suspending the same virtual machine.
   *
   * @param thunk The block of code to execute
   * @tparam T The type of result from the block of code
   *
   * @return The results of attempting to execute the block of code
   */
  protected def suspendVirtualMachineAndExecute[T](thunk: => T) = synchronized {
    // Pause the virtual machine while we perform the operation
    _virtualMachine.suspend()

    // Execute the operation
    val result = Try(thunk)

    // Resume the virtual machine now that the operation has been completed
    _virtualMachine.resume()

    // Return the result of the operation
    result
  }
}
