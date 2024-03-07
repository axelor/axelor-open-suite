package com.axelor.apps.account.service.reconcilegroup;

import com.axelor.apps.account.db.ReconcileGroup;
import com.axelor.apps.base.AxelorException;

public interface ReconcileGroupLetterService {

  /**
   * Letter every moveline and update lettering date.
   *
   * @param reconcileGroup
   * @throws AxelorException
   */
  void letter(ReconcileGroup reconcileGroup) throws AxelorException;
}
