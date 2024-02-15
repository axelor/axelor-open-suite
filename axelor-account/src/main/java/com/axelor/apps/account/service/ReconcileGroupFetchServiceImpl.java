package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.google.inject.Inject;
import java.util.List;

public class ReconcileGroupFetchServiceImpl implements ReconcileGroupFetchService {

  @Inject
  public ReconcileGroupFetchServiceImpl(ReconcileRepository reconcileRepository) {
    this.reconcileRepository = reconcileRepository;
  }

  protected ReconcileRepository reconcileRepository;

  @Override
  public List<Reconcile> fetchConfirmedReconciles(ReconcileGroup reconcileGroup) {
    return reconcileRepository
        .all()
        .filter("self.reconcileGroup.id = :reconcileGroupId AND self.statusSelect = :confirmed")
        .bind("reconcileGroupId", reconcileGroup.getId())
        .bind("confirmed", ReconcileRepository.STATUS_CONFIRMED)
        .fetch();
  }
}
