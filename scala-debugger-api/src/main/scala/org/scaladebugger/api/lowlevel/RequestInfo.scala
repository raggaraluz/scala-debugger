package org.scaladebugger.api.lowlevel
import acyclic.file

/**
 * Represents generic information about a request.
 */
trait RequestInfo {
  /** The id of the request. */
  val requestId: String

  /** Whether or not this request is pending (not on remote JVM). */
  val isPending: Boolean
}
