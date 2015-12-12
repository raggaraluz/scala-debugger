package org.senkbeil.debugger.api.lowlevel.monitors

import org.senkbeil.debugger.api.utils.PendingActionManager

/**
 * Provides pending monitor contended enter capabilities to an existing
 * monitor contended enter manager.
 *
 * Contains an internal pending action manager.
 */
trait StandardPendingMonitorContendedEnterSupport extends PendingMonitorContendedEnterSupport {
  override protected val pendingActionManager: PendingActionManager[MonitorContendedEnterRequestInfo] =
    new PendingActionManager[MonitorContendedEnterRequestInfo]
}
