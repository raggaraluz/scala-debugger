package org.senkbeil.debugger.api.lowlevel.monitors

import org.senkbeil.debugger.api.utils.PendingActionManager

/**
 * Provides pending monitor wait capabilities to an existing
 * monitor wait manager.
 *
 * Contains an internal pending action manager.
 */
trait StandardPendingMonitorWaitSupport extends PendingMonitorWaitSupport {
  override protected val pendingActionManager: PendingActionManager[MonitorWaitRequestInfo] =
    new PendingActionManager[MonitorWaitRequestInfo]
}
