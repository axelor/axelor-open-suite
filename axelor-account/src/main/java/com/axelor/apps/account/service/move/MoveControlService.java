package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;

public interface MoveControlService {

  /**
   * Method that checks if move.company is the same used in journal.
   *
   * @param move
   */
  void checkSameCompany(Move move) throws AxelorException;
}
