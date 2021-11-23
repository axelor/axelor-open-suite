package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;

public interface MoveLineControlService {

  /**
   * Method that control if accountingAccount of line is a valid or not.
   *
   * @param line
   * @throws AxelorException if line is not valid.
   */
  void controlAccountingAccount(MoveLine line) throws AxelorException;

  void validateMoveLine(MoveLine moveLine) throws AxelorException;

  Move setMoveLineDates(Move move) throws AxelorException;
}
