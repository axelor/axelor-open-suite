package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;

public interface ForeignExchangeGapService {

  ForeignMoveToReconcile manageForeignExchangeGap(Reconcile reconcile) throws AxelorException;

  // TODO remove and set this methodad protected
  boolean checkForeignExchangeAccounts(Company company) throws AxelorException;

  void unreconcileForeignExchangeMove(Reconcile reconcile) throws AxelorException;
}
