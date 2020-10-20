package com.axelor.csv.script;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.service.ReconcileServiceImpl;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.util.Map;

public class ImportReconcile {

  private ReconcileServiceImpl reconcileService;

  @Inject
  public ImportReconcile(ReconcileServiceImpl reconcileService) {
    this.reconcileService = reconcileService;
  }

  public Object updateInvoicePayments(Object bean, Map<String, Object> values)
      throws AxelorException {
    assert bean instanceof Reconcile;
    Reconcile reconcile = (Reconcile) bean;
    if (reconcile.getStatusSelect() == ReconcileRepository.STATUS_CONFIRMED) {
      reconcileService.updateInvoicePayments(reconcile);
    }
    return reconcile;
  }
}
