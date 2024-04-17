package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;

public interface ForeignExchangeGapService {

  Move manageForeignExchangeGap(Reconcile reconcile) throws AxelorException;

  boolean isGain(MoveLine creditMoveLine, MoveLine debitMoveLine, boolean isDebit);

  boolean isDebit(MoveLine creditMoveLine, MoveLine debitMoveLine);

  boolean checkForeignExchangeAccounts(Company company) throws AxelorException;

  void unreconcileForeignExchangeMove(Reconcile reconcile) throws AxelorException;

  InvoicePayment createForeignExchangeInvoicePayment(Reconcile newReconcile, Reconcile reconcile)
      throws AxelorException;
}
