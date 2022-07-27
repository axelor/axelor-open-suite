package com.axelor.apps.account.service.move.control.accounting.account;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;

public interface MoveAccountingAccountControlService {

  /**
   * Method that checks inactive account
   *
   * @param move
   * @throws AxelorException
   */
  void checkInactiveAccount(Move move) throws AxelorException;
}
