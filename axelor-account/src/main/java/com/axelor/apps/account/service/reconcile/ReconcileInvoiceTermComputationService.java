package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.base.AxelorException;

public interface ReconcileInvoiceTermComputationService {

  void updatePayments(Reconcile reconcile, boolean updateInvoiceTerms) throws AxelorException;
}
