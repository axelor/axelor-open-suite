package com.axelor.apps.account.service.reconcile.foreignexchange;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;

public interface ForeignExchangeGapService {

  ForeignMoveToReconcile manageForeignExchangeGap(Reconcile reconcile) throws AxelorException;

  void unreconcileForeignExchangeMove(Reconcile reconcile) throws AxelorException;

  void adjustReconcileAmount(
      Reconcile reconcile,
      Invoice invoice,
      InvoicePayment invoicePayment,
      BigDecimal foreignExchangeReconciledAmount);
}
