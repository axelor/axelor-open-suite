package com.axelor.apps.account.service.reconcilegroup;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.service.reconcile.UnreconcileService;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class ReconcileGroupUnletterServiceImpl implements ReconcileGroupUnletterService {

  UnreconcileService unReconcileService;
  ReconcileGroupFetchService reconcileGroupFetchService;

  @Inject
  public ReconcileGroupUnletterServiceImpl(
      UnreconcileService unReconcileService,
      ReconcileGroupFetchService reconcileGroupFetchService) {
    this.unReconcileService = unReconcileService;
    this.reconcileGroupFetchService = reconcileGroupFetchService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void unletter(ReconcileGroup reconcileGroup) throws AxelorException {
    List<Reconcile> reconcileList =
        reconcileGroupFetchService.fetchConfirmedReconciles(reconcileGroup);

    for (Reconcile reconcile : reconcileList) {
      unReconcileService.unreconcile(reconcile);
    }
  }
}
