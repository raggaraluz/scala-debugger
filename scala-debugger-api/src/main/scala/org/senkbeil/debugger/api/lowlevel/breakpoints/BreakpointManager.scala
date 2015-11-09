package org.senkbeil.debugger.api.lowlevel.breakpoints

import java.util.concurrent.ConcurrentHashMap

import com.sun.jdi.request.{EventRequestManager, BreakpointRequest}
import org.senkbeil.debugger.api.lowlevel.classes.ClassManager
import org.senkbeil.debugger.api.lowlevel.requests.JDIRequestArgument
import org.senkbeil.debugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.utils.Logging
import com.sun.jdi.Location

import scala.collection.mutable
import scala.collection.JavaConverters._
import scala.util.{Try, Failure, Success}

/**
 * Represents the manager for breakpoint requests.
 *
 * @param eventRequestManager The manager used to create breakpoint requests
 * @param classManager The class manager associated with the virtual machine,
 *                      used to retrieve location information
 */
class BreakpointManager(
  private val eventRequestManager: EventRequestManager,
  private val classManager: ClassManager
) extends Logging {
  import org.senkbeil.debugger.api.lowlevel.requests.Implicits._

  type BreakpointKey = (String, Int) // Class Name, Line Number
  private var lineBreakpoints = Map[BreakpointKey, Seq[BreakpointRequest]]()

  private case class BreakpointInfo(
    fileName: String,
    lineNumber: Int,
    extraArguments: Seq[JDIRequestArgument]
  )
  private val pendingLineBreakpoints: mutable.Map[String, Seq[BreakpointInfo]] =
    new ConcurrentHashMap[String, Seq[BreakpointInfo]]().asScala

  /**
   * Retrieves the list of breakpoints contained by this manager.
   *
   * @return The collection of breakpoints in the form of
   *         (class name, line number)
   */
  def breakpointRequestList: Seq[BreakpointKey] = lineBreakpoints.keys.toSeq

  /**
   * Processes pending breakpoint requests for the specified file name.
   *
   * @param fileName The name of the file whose pending breakpoints to process
   *
   * @return True if all pending breakpoints for the file were successfully
   *         added, otherwise false
   */
  def processPendingBreakpoints(fileName: String): Boolean = {
    def tryBreakpoint(breakpointInfo: BreakpointInfo) = {
      createLineBreakpointRequest(
        fileName = breakpointInfo.fileName,
        lineNumber = breakpointInfo.lineNumber,
        breakpointInfo.extraArguments: _*
      )
    }

    // Process all breakpoints
    pendingLineBreakpoints.remove(fileName).foreach(_.foreach(tryBreakpoint))

    // Indicate whether or not we successfully added all of the breakpoints
    !pendingLineBreakpoints.contains(fileName)
  }

  /**
   * Creates and enables a breakpoint on the specified line of the class. If
   * the file/location is not found, the breakpoint will be placed on a pending
   * breakpoint list to be tried later.
   *
   * @param fileName The name of the file to set a breakpoint
   * @param lineNumber The number of the line to break
   * @param extraArguments The additional arguments to provide to the breakpoint
   *                       request
   *
   * @return True if successfully added breakpoints, otherwise false
   */
  def createLineBreakpointRequest(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIRequestArgument*
  ): Try[Boolean] = {
    val arguments = Seq(
      SuspendPolicyProperty.EventThread,
      EnabledProperty(value = true)
    ) ++ extraArguments
    val result = createLineBreakpointRequestImpl(
      fileName,
      lineNumber,
      arguments
    )

    // Add the attempt to our list for processing later if no exception was
    // thrown but we were not able to find the location
    if (result.isSuccess && !result.get) {
      pendingLineBreakpoints.synchronized {
        val oldPendingBreakpoints =
          pendingLineBreakpoints.getOrElse(fileName, Nil)
        val newPendingBreakpoint = BreakpointInfo(
          fileName        = fileName,
          lineNumber      = lineNumber,
          extraArguments  = extraArguments
        )

        pendingLineBreakpoints.put(
          fileName, oldPendingBreakpoints :+ newPendingBreakpoint)
      }
    }

    result
  }

  /**
   * Creates and enables a breakpoint on the specified line of the class.
   *
   * @param fileName The name of the file to set a breakpoint
   * @param lineNumber The number of the line to break
   * @param arguments All arguments for the request
   *
   * @return True if successfully added breakpoints, otherwise false
   */
  private def createLineBreakpointRequestImpl(
    fileName: String,
    lineNumber: Int,
    arguments: Seq[JDIRequestArgument]
  ): Try[Boolean] = {
    // Retrieve the available locations for the specified line
    val locations = classManager
      .linesAndLocationsForFile(fileName)
      .flatMap(_.get(lineNumber))
      .getOrElse(Nil)

    // Exit early if no locations are available
    if (locations.isEmpty) return Success(false)

    // Create and enable breakpoints for all underlying locations
    val result = Try {
      // Our key is using the class name and line number relevant to the
      // line breakpoint
      val key: BreakpointKey = (fileName, lineNumber)

      // Build our collection of breakpoints representing the overall breakpoint
      // TODO: Need to undo this if creating a request failed
      val innerBreakpoints = locations.map(
        eventRequestManager.createBreakpointRequest(_: Location, arguments: _*)
      )

      // Add the inner breakpoints to our list of line breakpoints
      lineBreakpoints += key -> innerBreakpoints

      // Indicate success
      true
    }

    result match {
      case Success(_) => logger.trace(s"Added breakpoint $fileName:$lineNumber")
      case Failure(ex) => logger.throwable(ex)
    }

    result
  }

  /**
   * Determines whether or not the breakpoint for the specific file's line.
   *
   * @param fileName The name of the file whose line to reference
   * @param lineNumber The number of the line to check for a breakpoint
   *
   * @return True if a breakpoint exists, otherwise false
   */
  def hasLineBreakpointRequest(fileName: String, lineNumber: Int): Boolean =
    lineBreakpoints.contains((fileName, lineNumber))

  /**
   * Returns the collection of breakpoints representing the breakpoint for the
   * specified line.
   *
   * @param fileName The name of the file whose line to reference
   * @param lineNumber The number of the line to check for breakpoints
   *
   * @return Some collection of breakpoints for the specified line, or None if
   *         the specified line has no breakpoints
   */
  def getLineBreakpointRequest(
    fileName: String,
    lineNumber: Int
  ): Option[Seq[BreakpointRequest]] = lineBreakpoints.get((fileName, lineNumber))

  /**
   * Removes the breakpoint on the specified line of the file.
   *
   * @param fileName The name of the file to remove the breakpoint
   * @param lineNumber The number of the line to break
   *
   * @return True if successfully removed breakpoint, otherwise false
   */
  def removeLineBreakpointRequest(
    fileName: String,
    lineNumber: Int
  ): Boolean = {
    // Remove breakpoints for all underlying locations
    val result = Try {
      val key: BreakpointKey = (fileName, lineNumber)

      val requestsToRemove = lineBreakpoints(key)

      eventRequestManager.deleteEventRequests(requestsToRemove.asJava)
    }

    result match {
      case Success(_) =>
        logger.trace(s"Removed breakpoint $fileName:$lineNumber")
      case Failure(ex) =>
        logger.throwable(ex)
    }

    result.isSuccess
  }
}
