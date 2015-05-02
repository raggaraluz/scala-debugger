package com.senkbeil.debugger.classes

import com.senkbeil.debugger.jdi.JDIHelperMethods
import com.senkbeil.utils.LogLike
import com.sun.jdi.{Location, ReferenceType, VirtualMachine}

import scala.collection.JavaConverters._
import scala.util.Try

import ClassManager._

/**
 * Represents the container for constants used in the class manager.
 */
object ClassManager {
  /** Used as the "file name" for classes with no 'file' that match arrays. */
  val DefaultArrayGroupName = "ARRAY"

  /** Used as the "file name" for classes with no 'file' that match nothing. */
  val DefaultUnknownGroupName = "UNKNOWN"
}

/**
 * Represents a manager of classes available on the virtual machine and their
 * associated files.
 *
 * @param _virtualMachine The virtual machine whose classes to manage
 * @param loadClasses Whether or not to load all classes from the virtual
 *                    machine on initialization of this manager
 */
class ClassManager(
  protected val _virtualMachine: VirtualMachine,
  loadClasses: Boolean = true
) extends JDIHelperMethods with LogLike {
  /** Mapping of file names to associated classes. */
  private var fileToClasses: Map[String, Seq[ReferenceType]] = Map()

  /**
   * Retrieves the mapping of lines to locations available for a specific file.
   *
   * @param fileName The name of the file whose lines and underlying
   *                  locations to retrieve
   *
   * @return The mapping of file lines to associated locations in underlying
   *         JVM classes
   */
  def linesAndLocationsForFile(fileName: String): Map[Int, Seq[Location]] = {
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
    underlyingReferencesForFile(fileName)
      .map(linesForReferenceType)
      .reduce(_ ++ _)
      .groupBy(_.lineNumber())
  }

  /**
   * Retrieves the list of underlying JVM classes for the specified file.
   *
   * @param fileName The name of the file whose underlying representations
   *                  to retrieve
   *
   * @return The list of underlying class references
   */
  def underlyingReferencesForFile(fileName: String): Seq[ReferenceType] = {
    require(allFileNames.contains(fileName), s"$fileName not found!")

    fileToClasses(fileName)
  }

  /**
   * Refresh the list of classes contained by the underlying virtual machine.
   * Groups by source path, falling back to a standard "ARRAY" grouping for
   * references to array structures and "UNKNOWN" for references with no
   * source name or known name.
   */
  def refreshAllClasses(): ClassManager = {
    fileToClasses = _virtualMachine.allClasses().asScala
      .groupBy { referenceType =>
        Try(sourcePath(referenceType)).getOrElse(
          if (referenceType.name().endsWith("[]")) DefaultArrayGroupName
          else DefaultUnknownGroupName
        )
      }

    this
  }

  /**
   * Retrieves a list of available Scala file names.
   *
   * @return The collection of file names
   */
  def allScalaFileNames: Seq[String] =
    allFileNamesWithExtension("scala")

  /**
   * Retrieves a list of available Java file names.
   *
   * @return The collection of file names
   */
  def allJavaFileNames: Seq[String] =
    allFileNamesWithExtension("java")

  /**
   * Retrieves a list of available file names with the provided extension.
   *
   * @param extension The extension of the file names (Scala/Java/etc)
   *
   * @return The collection of file names
   */
  def allFileNamesWithExtension(extension: String): Seq[String] =
      allFileNames.filter(_.endsWith(extension))

  /**
   * Retrieves a list of all available file names.
   *
   * @return The collection of file names
   */
  def allFileNames: Seq[String] = fileToClasses.keys.toSeq

  // ==========================================================================
  // = CONSTRUCTOR
  // ==========================================================================

  // If marked to load classes during the constructor, do so now
  if (loadClasses) refreshAllClasses()
}
