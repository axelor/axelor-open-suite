package com.axelor.apps.account.service.move.control.moveline;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;

public interface MoveLineChangeControlService {

  /**
   * Check if the move line should not be deleted or not.
   *
   * @param moveLine
   * @throws AxelorException
   */
  void checkIllegalRemoval(MoveLine moveLine) throws AxelorException;
}
