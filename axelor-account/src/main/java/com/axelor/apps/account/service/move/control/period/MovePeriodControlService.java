package com.axelor.apps.account.service.move.control.period;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;

public interface MovePeriodControlService {

  /**
   * Method thats check if the period of the move is closed.
   *
   * @param move
   * @throws AxelorException
   */
  void checkClosedPeriod(Move move) throws AxelorException;

  /**
   * Method that checks if user has authorization on this move.
   *
   * @param move
   * @throws AxelorException
   */
  void checkAuthorizationOnClosedPeriod(Move move) throws AxelorException;
}
