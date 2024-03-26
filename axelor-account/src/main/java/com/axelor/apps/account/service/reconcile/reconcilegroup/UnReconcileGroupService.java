package com.axelor.apps.account.service.reconcile.reconcilegroup;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.base.AxelorException;

public interface UnReconcileGroupService {

  /**
   * Remove a reconcile from a reconcile group then update the group.
   *
   * @param reconcile a reconcile with a reconcile group.
   */
  void remove(Reconcile reconcile) throws AxelorException;
}
