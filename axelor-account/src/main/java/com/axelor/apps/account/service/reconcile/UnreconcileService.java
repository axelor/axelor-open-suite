package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.base.AxelorException;

public interface UnreconcileService {

  void unreconcile(Reconcile reconcile) throws AxelorException;
}
