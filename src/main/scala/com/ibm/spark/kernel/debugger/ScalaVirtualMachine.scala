package com.ibm.spark.kernel.debugger

import com.sun.jdi._

import scala.util.Try
import scala.collection.JavaConverters._

class ScalaVirtualMachine(private val _virtualMachine: VirtualMachine) {
  private val DefaultArrayGroupName = "ARRAY"
  private val DefaultUnknownGroupName = "UNKNOWN"

  private val eventRequestManager = _virtualMachine.eventRequestManager()

  private var allClasses: Map[String, Seq[ReferenceType]] = Map()

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
   * Retrieves the list of underlying JVM classes for the specified class.
   *
   * @param className The name of the class whose underlying representations
   *                  to retrieve
   *
   * @return The list of underlying class references
   */
  private def underlyingReferencesFor(className: String) = {
    require(allClassNames.contains(className), s"$className not found!")

    val sourceName = className + ".scala"

    allClasses(sourceName)
  }

  /**
   * Retrieves a list of abstracted class names (not underlying Java classes).
   *
   * @return The list of names
   */
  def allClassNames: Seq[String] = {
    refreshAllClasses()
    allClasses
      .filter { case (key, _) => key.endsWith("scala") }
      .map { case (key, _) => key.substring(0, key.lastIndexOf('.')) }
      .toSeq
  }

  /**
   * Retrieves the mapping of lines to locations available for a specific class.
   *
   * @param className The name of the class whose lines and underlying
   *                  locations to retrieve
   *
   * @return The mapping of class lines to associated locations in underlying
   *         JVM classes
   */
  private def linesAndLocationsForClass(className: String) = {
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
   * Retrieves the list of available lines for a specific class.
   *
   * @param className The name of the class whose lines to retrieve
   *
   * @return The list of breakpointable lines
   */
  def availableLinesForClass(className: String): Seq[Int] =
    linesAndLocationsForClass(className).keys.toSeq

  /**
   * Retrieves the static fields and their values for the specified class.
   *
   * @param className The name of the class whose static fields to retrieve
   *
   * @return The list of static fields and their respective values
   */
  def staticFieldsForClass(className: String): Seq[(Field, Value)] = {
    underlyingReferencesFor(className)
      .map { ref =>
        (ref, Try(ref.allFields()).map(_.asScala).getOrElse(Nil))
      } map { case (ref, fields) =>
        val staticFields =
          fields.filter(field => Try(ref.getValue(field)).isSuccess)

        (ref, staticFields)
      } map { case (ref, staticFields) =>
        staticFields.map(field => (field, ref.getValue(field)))
      } reduce { _ ++ _ }
  }

  /**
   * Creates and enables a breakpoint on the specified line of the class.
   *
   * @param className The name of the class to set a breakpoint
   * @param lineNumber The number of the line to break
   */
  def setBreakpointOnClassLine(className: String, lineNumber: Int) = {
    // Retrieve the available locations for the specified line
    val locations = {
      val linesAndLocations = linesAndLocationsForClass(className)
      require(linesAndLocations.contains(lineNumber),
        s"$lineNumber is not an available line for $className!")

      linesAndLocations(lineNumber)
    }

    // Create an enable breakpoints for all underlying locations
    locations
      .map(eventRequestManager.createBreakpointRequest)
      .foreach(_.setEnabled(true))
  }
}

