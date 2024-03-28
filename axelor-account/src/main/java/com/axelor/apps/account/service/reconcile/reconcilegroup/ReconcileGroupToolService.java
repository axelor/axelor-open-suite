package com.axelor.apps.account.service.reconcile.reconcilegroup;

import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.base.AxelorException;
import java.util.List;

public interface ReconcileGroupToolService {

  boolean isBalanced(List<Reconcile> reconcileList);

  void validate(ReconcileGroup reconcileGroup, List<Reconcile> reconcileList)
      throws AxelorException;
}
