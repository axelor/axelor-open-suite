package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.MoveLine;

public interface AnalyticMoveLineGenerateRealService {

  /**
   * Generate a real analytic move line from a forecast move line, by copying the forecast analytic
   * move line and updating the links to related invoice and move line.
   *
   * @param forecastAnalyticMoveLine a forecast analytic move line that will be copied.
   * @param moveLine the move line that will be linked to the created analytic move line.
   * @return the created real analytic move line
   */
  AnalyticMoveLine createFromForecast(AnalyticMoveLine forecastAnalyticMoveLine, MoveLine moveLine);
}
