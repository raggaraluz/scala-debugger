package org.senkbeil.debugger.api.lowlevel

/**
 * Represents generic information about a request.
 */
trait RequestInfo {
  /** The id of the request. */
  val requestId: String
}
