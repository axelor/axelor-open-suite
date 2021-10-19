package com.axelor.apps.account.service;

import com.axelor.apps.base.db.Year;
import com.axelor.exception.AxelorException;

public interface YearControlService {

  /**
   * Method that control dates (fromDate and toDate). In case a changement occured while this year
   * is used in a Period that is used in move it will throw a Exception.
   *
   * @param asType
   * @throws AxelorException
   */
  void controlDates(Year year) throws AxelorException;
}
