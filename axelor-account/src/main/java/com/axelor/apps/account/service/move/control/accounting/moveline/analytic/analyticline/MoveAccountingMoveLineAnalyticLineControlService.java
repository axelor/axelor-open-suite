package com.axelor.apps.account.service.move.control.accounting.moveline.analytic.analyticline;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.exception.AxelorException;

public interface MoveAccountingMoveLineAnalyticLineControlService {

  /**
   * Method that checks inactive analytic journal
   *
   * @param analyticMoveLine
   * @throws AxelorException
   */
  void checkInactiveAnalyticJournal(AnalyticMoveLine analyticMoveLine) throws AxelorException;

  /**
   * Method that checks inactive analytic account
   *
   * @param analyticMoveLine
   * @throws AxelorException
   */
  void checkInactiveAnalyticAccount(AnalyticMoveLine analyticMoveLine) throws AxelorException;
}
