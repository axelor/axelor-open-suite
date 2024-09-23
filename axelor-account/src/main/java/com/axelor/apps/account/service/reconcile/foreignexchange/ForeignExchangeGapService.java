package com.axelor.apps.account.service.reconcile.foreignexchange;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.base.AxelorException;

public interface ForeignExchangeGapService {

  ForeignMoveToReconcile manageForeignExchangeGap(Reconcile reconcile) throws AxelorException;

  void unreconcileForeignExchangeMove(Reconcile reconcile) throws AxelorException;
}
