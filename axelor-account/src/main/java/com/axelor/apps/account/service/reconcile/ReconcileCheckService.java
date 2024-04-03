package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.base.AxelorException;

public interface ReconcileCheckService {

  void reconcilePreconditions(
      Reconcile reconcile, boolean updateInvoicePayments, boolean updateInvoiceTerm)
      throws AxelorException;

  void checkCurrencies(MoveLine debitMoveLine, MoveLine creditMoveLine) throws AxelorException;

  boolean isCompanyCurrency(Reconcile reconcile, InvoicePayment invoicePayment, Move otherMove);

  void checkReconcile(Reconcile reconcile) throws AxelorException;
}
