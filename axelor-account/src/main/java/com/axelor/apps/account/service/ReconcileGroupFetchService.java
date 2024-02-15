package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.ReconcileGroup;
import java.util.List;

public interface ReconcileGroupFetchService {

  /**
   * Fetch reconciles related to given reconcile group that have the status confirmed.
   *
   * @param reconcileGroup a reconcile group with an id
   * @return the list of found reconcile
   */
  List<Reconcile> fetchConfirmedReconciles(ReconcileGroup reconcileGroup);
}
