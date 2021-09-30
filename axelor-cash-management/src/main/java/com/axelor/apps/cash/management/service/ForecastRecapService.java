package com.axelor.apps.cash.management.service;

import com.axelor.apps.cash.management.db.ForecastRecap;
import com.axelor.apps.cash.management.db.ForecastRecapLineType;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

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

  public void getOpportunities(
      ForecastRecap forecastRecap,
      Map<LocalDate, BigDecimal> mapExpected,
      Map<LocalDate, BigDecimal> mapConfirmed)
      throws AxelorException;

  public void getInvoices(
      ForecastRecap forecastRecap,
      Map<LocalDate, BigDecimal> mapExpected,
      Map<LocalDate, BigDecimal> mapConfirmed);

  public void getTimetablesOrOrders(
      ForecastRecap forecastRecap,
      Map<LocalDate, BigDecimal> mapExpected,
      Map<LocalDate, BigDecimal> mapConfirmed)
      throws AxelorException;

  public void getForecasts(
      ForecastRecap forecastRecap,
      Map<LocalDate, BigDecimal> mapExpected,
      Map<LocalDate, BigDecimal> mapConfirmed);
}
