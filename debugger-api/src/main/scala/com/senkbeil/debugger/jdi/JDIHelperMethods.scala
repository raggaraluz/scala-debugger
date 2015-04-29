package com.senkbeil.debugger.jdi

import com.senkbeil.utils.LogLike
import com.sun.jdi.{ReferenceType, ThreadReference, VirtualMachine}

import scala.collection.JavaConverters._
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

  /**
   * Determines the main executing thread of the _virtualMachine instance.
   *
   * @return The reference to the main thread
   */
  protected def findMainThread(): ThreadReference =
    findMainThread(_virtualMachine)

  /**
   * Determines the main executing thread of the specified virtual machine.
   *
   * @param virtualMachine The virtual machine whose main thread to determine
   *
   * @return The reference to the main thread
   */
  protected def findMainThread(virtualMachine: VirtualMachine) =
    virtualMachine.allThreads().asScala.find(_.name() == "main").get

  /**
   * Retrieves the convergent source path of the provided reference type.
   *
   * @example package.SomeClass$ will return package.SomeClass
   *
   * @param referenceType The reference instance whose source path to determine
   *
   * @throws AssertionError If the source paths for the reference type are not
   *                        convergent (basically, not the same)
   *
   * @return The source path as a string
   */
  protected def sourcePath(referenceType: ReferenceType): String = {
    val sourcePaths =
      referenceType.sourcePaths(_virtualMachine.getDefaultStratum).asScala

    val sourcePath = sourcePaths.foldLeft(sourcePaths.head) {
      case (a, b) =>
        // If we have different paths, there is no way to determine a full
        // original class name
        assert(a == b, "Source paths are divergent!")

        // Should all be the same
        b
    }

    sourcePath
  }
}
