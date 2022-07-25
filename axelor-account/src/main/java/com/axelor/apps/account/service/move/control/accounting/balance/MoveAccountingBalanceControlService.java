package com.axelor.apps.account.service.move.control.accounting.balance;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;

public interface MoveAccountingBalanceControlService {

  /**
   * Checks that the move is balanced
   *
   * @param move: Move
   * @throws AxelorException
   */
  void checkWellBalanced(Move move) throws AxelorException;
}
