package com.ibm.spark.kernel.debugger

import java.io.File

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

  /**
   * Converts a source path to a fully-qualified class name.
   *
   * @example scala/util/Try.scala becomes scala.util.Try
   *
   * @param sourcePath The source path to convert
   *
   * @return The fully-qualified class name as a string
   */
  private def sourcePathToFullClassName(sourcePath: String): String = {
    val fullSourcePlusExtension = sourcePath.replace(File.separatorChar, '.')
    fullSourcePlusExtension.substring(
      0, fullSourcePlusExtension.lastIndexOf(".scala")
    )
  }

  /**
   * Retrieves the full class name (java.util.File) of the original instance in
   * Scala rather than the fragmented creations when going from Scala to Java.
   *
   * @example package.SomeClass$ will return package.SomeClass
   *
   * @param referenceType The reference instance whose original class name to
   *                      determine
   *
   * @return The fully-quantified original class name
   */
  protected def fullOriginalClassName(referenceType: ReferenceType): String = {
    val sourcePaths =
      referenceType.sourcePaths(_virtualMachine.getDefaultStratum).asScala

    val sourcePath = sourcePaths.foldLeft(sourcePaths.head) { case (a, b) =>
      // If we have different paths, there is no way to determine a full
      // original class name
      require(a == b, "Source paths are divergent!")

      // Should all be the same
      b
    }

    sourcePathToFullClassName(sourcePath)
  }
}
