package com.axelor.apps.account.service.move.control.moveline.amount;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;

public interface MoveLineAmountControlService {

  /**
   * Method that checks that moveLine amount are not all empty (debit, credit, amount currency).
   *
   * @param moveLine
   * @throws AxelorException
   */
  void checkNotEmpty(MoveLine moveLine) throws AxelorException;
}
