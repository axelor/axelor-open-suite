package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;

public interface MoveSimulateService {
  /**
   * Check if simulate conditions are met and set move to simulate status
   *
   * @param move
   * @return
   * @throws AxelorException
   */
  void simulate(Move move) throws AxelorException;
}
