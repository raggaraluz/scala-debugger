package com.ibm.spark.kernel.debugger

import com.sun.jdi.{VirtualMachine, Location, ReferenceType}
import collection.JavaConverters._

import scala.util.Try

class ClassManager(protected val _virtualMachine: VirtualMachine)
  extends JDIHelperMethods
{
  private val DefaultArrayGroupName = "ARRAY"
  private val DefaultUnknownGroupName = "UNKNOWN"

  private var allClasses: Map[String, Seq[ReferenceType]] = Map()

  /**
   * Retrieves the mapping of lines to locations available for a specific class.
   *
   * @param className The name of the class whose lines and underlying
   *                  locations to retrieve
   *
   * @return The mapping of class lines to associated locations in underlying
   *         JVM classes
   */
  def linesAndLocationsForClass(className: String) = {
    /**
     * Retrieve the available locations for the specified reference type.
     *
     * @param referenceType The reference type whose locations to retrieve
     *
     * @return The list of available locations
     */
    def linesForReferenceType(referenceType: ReferenceType): Seq[Location] = {
      Try(referenceType.allLineLocations())
        .map(_.asScala).getOrElse(Nil)
        .filter(location => Try(location.lineNumber()).isSuccess)
    }

    // Combine all lines for underlying reference types together
    underlyingReferencesFor(className)
      .map(linesForReferenceType)
      .reduce(_ ++ _)
      .groupBy(_.lineNumber())
  }

  /**
   * Retrieves the list of underlying JVM classes for the specified class.
   *
   * @param className The name of the class whose underlying representations
   *                  to retrieve
   *
   * @return The list of underlying class references
   */
  def underlyingReferencesFor(className: String) = {
    require(allClassNames(refresh = true).contains(className),
      s"$className not found!")

    val sourceName = className + ".scala"

    allClasses(sourceName)
  }


  /**
   * Refresh the list of classes contained by the underlying virtual machine.
   * Groups by source name, falling back to a standard "ARRAY" grouping for
   * references to array structures and "UNKNOWN" for references with no
   * source name or known name.
   */
  private def refreshAllClasses() =
    allClasses = _virtualMachine.allClasses().asScala
      .groupBy(referenceType => Try(referenceType.sourceName()).getOrElse(
      if (referenceType.name().endsWith("[]")) DefaultArrayGroupName
      else DefaultUnknownGroupName
    ))

  /**
   * Retrieves a list of abstracted class names (not underlying Java classes).
   *
   * @param refresh If true, refreshes the internal cache of classes known to
   *                be present in the virtual machine
   *
   * @return The list of names
   */
  def allClassNames(refresh: Boolean = true): Seq[String] = {
    if (refresh) refreshAllClasses()
    allClasses
      .filter { case (key, _) => key.endsWith("scala") }
      .map { case (key, _) => key.substring(0, key.lastIndexOf('.')) }
      .toSeq
  }
}
