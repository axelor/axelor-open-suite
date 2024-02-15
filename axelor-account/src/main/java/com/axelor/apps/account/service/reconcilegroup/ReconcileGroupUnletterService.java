package com.axelor.apps.account.service.reconcilegroup;

import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.base.AxelorException;

public interface ReconcileGroupUnletterService {

  /**
   * Unletter every moveline and update unlettering date.
   *
   * @param reconcileGroup
   * @throws AxelorException
   */
  void unletter(ReconcileGroup reconcileGroup) throws AxelorException;
}
