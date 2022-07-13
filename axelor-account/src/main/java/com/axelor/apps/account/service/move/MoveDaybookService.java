package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;

public interface MoveDaybookService {
  /**
   * Check if daybook conditions are met and set move to daybook status
   *
   * @param move
   * @return
   * @throws AxelorException
   */
  void daybook(Move move) throws AxelorException;
}
