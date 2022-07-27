package com.axelor.apps.account.service.move.control.accounting;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;

public interface MoveAccountingControlService {

  /**
   * This method will checks if move matches all the pre-conditions necessary in order to account
   * it. If not, this method will throw a exception.
   *
   * <p>This method is not intended to control the move lines of the move.
   *
   * @param move
   */
  void controlAccounting(Move move) throws AxelorException;

  /**
   * This method will checks if move and its move lines matches all the pre-conditions necessary in
   * order to account it. If not, this method will throw a exception.
   *
   * @param move
   */
  void deepControlAccounting(Move move) throws AxelorException;
}
