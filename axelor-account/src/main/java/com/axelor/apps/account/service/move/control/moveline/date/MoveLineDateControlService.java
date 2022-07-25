package com.axelor.apps.account.service.move.control.moveline.date;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;

public interface MoveLineDateControlService {

  /**
   * Check if the date of the move line is in the period of its move parent. (If it is not null)
   *
   * @param moveLine
   * @throws AxelorException
   */
  void checkDateInPeriod(MoveLine moveLine) throws AxelorException;
}
