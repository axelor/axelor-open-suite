package com.axelor.apps.cash.management.service;

import com.axelor.apps.cash.management.db.ForecastRecap;
import com.axelor.apps.cash.management.db.ForecastRecapLineType;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface ForecastRecapService {

  void reset(ForecastRecap forecastRecap);

  void finish(ForecastRecap forecastRecap);

  void populate(ForecastRecap forecastRecap) throws AxelorException;

  void createForecastRecapLine(
      LocalDate date,
      int type,
      BigDecimal amount,
      String relatedToSelect,
      Long relatedToSelectId,
      String relatedToSelectName,
      ForecastRecapLineType forecastRecapLineType,
      ForecastRecap forecastRecap);

  void computeForecastRecapLineBalance(ForecastRecap forecastRecap);

  String getForecastRecapFileLink(ForecastRecap forecastRecap, String reportType)
      throws AxelorException;
}
