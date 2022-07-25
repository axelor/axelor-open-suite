package com.axelor.apps.account.service.move.control;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;

public interface MoveChangeControlService {

  /**
   * This method will check if the move has done illegal removal by comparing it with the persisted
   * move (it is exist)
   *
   * @param move
   * @throws AxelorException
   */
  void checkIllegalRemoval(Move move) throws AxelorException;
}
