package org.senkbeil.debugger.api.lowlevel.watchpoints

import org.senkbeil.debugger.api.utils.PendingActionManager

/**
 * Provides pending access watchpoint capabilities to an existing access
 * watchpoint manager.
 *
 * Contains an internal pending action manager.
 */
trait StandardPendingAccessWatchpointSupport extends PendingAccessWatchpointSupport {
  override protected val pendingActionManager: PendingActionManager[AccessWatchpointRequestInfo] =
    new PendingActionManager[AccessWatchpointRequestInfo]
}
