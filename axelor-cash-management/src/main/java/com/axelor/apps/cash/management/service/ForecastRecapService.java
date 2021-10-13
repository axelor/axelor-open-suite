package com.axelor.apps.cash.management.service;

import com.axelor.apps.cash.management.db.ForecastRecap;
import com.axelor.exception.AxelorException;

public interface ForecastRecapService {

  void reset(ForecastRecap forecastRecap);

  void finish(ForecastRecap forecastRecap);

  void populate(ForecastRecap forecastRecap) throws AxelorException;

  void computeForecastRecapLineBalance(ForecastRecap forecastRecap);

  String getForecastRecapFileLink(ForecastRecap forecastRecap, String reportType)
      throws AxelorException;
}
