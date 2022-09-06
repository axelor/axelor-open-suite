package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.db.Year;
import com.axelor.exception.AxelorException;

public interface YearControlService {

  /**
   * Method that controls dates (fromDate and toDate). In case a changement occured while this year
   * is used in a Period that is used in move it will throw a Exception.
   *
   * @param asType
   * @throws AxelorException
   */
  void controlDates(Year year) throws AxelorException;

  /**
   * Method that checks if year is linked to {@link Move}
   *
   * @param year
   * @return true if year is linked, else false.
   */
  boolean isLinkedToMove(Year year);
}
