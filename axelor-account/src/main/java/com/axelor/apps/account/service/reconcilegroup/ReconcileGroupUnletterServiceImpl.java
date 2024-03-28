package com.axelor.apps.account.service.reconcilegroup;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.service.reconcile.UnReconcileService;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class ReconcileGroupUnletterServiceImpl implements ReconcileGroupUnletterService {

  UnReconcileService unReconcileService;
  ReconcileGroupFetchService reconcileGroupFetchService;

  @Inject
  public ReconcileGroupUnletterServiceImpl(
      UnReconcileService unReconcileService,
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
