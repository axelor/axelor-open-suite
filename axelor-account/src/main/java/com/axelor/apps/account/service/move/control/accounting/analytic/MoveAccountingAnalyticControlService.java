package com.axelor.apps.account.service.move.control.accounting.analytic;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;

public interface MoveAccountingAnalyticControlService {

  /**
   * Method that checks inactive analytic journal
   *
   * <p>This method will checks every analytic lines of every move lines.
   *
   * @param move
   * @throws AxelorException
   */
  void checkInactiveAnalyticJournal(Move move) throws AxelorException;

  /**
   * Method that checks inactive analytic account
   *
   * <p>This method will checks every analytic lines of every move lines.
   *
   * @param move
   * @throws AxelorException
   */
  void checkInactiveAnalyticAccount(Move move) throws AxelorException;
}
