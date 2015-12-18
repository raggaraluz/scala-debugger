package org.senkbeil.debugger.api.lowlevel.monitors

import org.senkbeil.debugger.api.utils.PendingActionManager

/**
 * Provides pending monitor contended entered capabilities to an existing
 * monitor contended entered manager.
 *
 * Contains an internal pending action manager.
 */
trait StandardPendingMonitorContendedEnteredSupport extends PendingMonitorContendedEnteredSupport {
  override protected val pendingActionManager: PendingActionManager[MonitorContendedEnteredRequestInfo] =
    new PendingActionManager[MonitorContendedEnteredRequestInfo]
}
