package com.axelor.apps.account.service.move.control;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;

public interface MovePreSaveControlService {

  /**
   * Control the validity of the move
   *
   * @param move
   * @throws AxelorException
   */
  void checkValidity(Move move) throws AxelorException;
}
