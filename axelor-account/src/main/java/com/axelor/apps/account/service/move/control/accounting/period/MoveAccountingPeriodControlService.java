package com.axelor.apps.account.service.move.control.accounting.period;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;

public interface MoveAccountingPeriodControlService {

  /**
   * Method that check if the period of the move is closed.
   *
   * @param move
   * @throws AxelorException
   */
  void checkClosedPeriod(Move move) throws AxelorException;
}
