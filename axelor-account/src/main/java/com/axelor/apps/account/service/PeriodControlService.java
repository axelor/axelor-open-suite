package com.axelor.apps.account.service;

import com.axelor.apps.base.db.Period;
import com.axelor.exception.AxelorException;

public interface PeriodControlService {

  /**
   * Method that control dates (fromDate and toDate) of period. It will throw an exception if a
   * changement occured while the period is linked to a move.
   *
   * @param period
   */
  void controlDates(Period period) throws AxelorException;

  /**
   * Checks if a Move is linked to period
   *
   * @param entity
   * @return
   */
  boolean isLinkedToMove(Period period);

  /**
   * Method that checks is statusSelect and year.statusSelect are OPENED.
   *
   * @param period
   * @return true if status select is OPENED, else false;
   */
  boolean isOpen(Period period);
}
