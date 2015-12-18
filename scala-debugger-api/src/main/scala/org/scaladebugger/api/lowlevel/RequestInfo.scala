package org.scaladebugger.api.lowlevel

/**
 * Represents generic information about a request.
 */
trait RequestInfo {
  /** The id of the request. */
  val requestId: String
}
