package com.axelor.apps.account.service.move.control.moveline.account;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;

public interface MoveLineAccountControlService {

  /**
   * Method that control if accountingAccount of line is a valid or not.
   *
   * @param line
   * @throws AxelorException if line is not valid.
   */
  void checkValidAccount(MoveLine moveLine) throws AxelorException;
}
