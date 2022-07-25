package com.axelor.apps.account.service.move.control.accounting.moveline.account;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;

public interface MoveAccountingMoveLineAccountControlService {

  /**
   * Method that control if accountingAccount of line is a valid or not.
   *
   * @param line
   * @throws AxelorException if line is not valid.
   */
  void checkValidAccount(MoveLine moveLine) throws AxelorException;
}
