package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.exception.AxelorException;

public interface AnalyticAxisControlService {

  /**
   * Method that checks unicity by code and company of analyticAxis.
   *
   * @param analyticAxis
   * @throws AxelorException
   */
  void controlUnicity(AnalyticAxis analyticAxis) throws AxelorException;

  /**
   * Method that checks if analyticAxis is in a {@link AnalyticMoveLine}
   *
   * @param analyticAxis
   * @return true if analyticAxis is used in a analyticMoveLine, false else.
   */
  boolean isInAnalyticMoveLine(AnalyticAxis analyticAxis);
}
