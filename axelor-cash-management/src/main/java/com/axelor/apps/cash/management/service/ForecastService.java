/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.cash.management.service;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.cash.management.db.Forecast;
import com.axelor.apps.cash.management.db.ForecastGenerator;
import com.axelor.apps.cash.management.db.ForecastRecapLineType;
import com.axelor.apps.cash.management.db.repo.ForecastRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ForecastService {

  @Inject protected AppBaseService appBaseService;
  @Inject protected ForecastRepository forecastRepo;

  @Transactional
  public void generate(ForecastGenerator forecastGenerator) {
    LocalDate fromDate = forecastGenerator.getFromDate();
    LocalDate toDate = forecastGenerator.getToDate();
    LocalDate itDate = LocalDate.parse(fromDate.toString(), DateTimeFormatter.ISO_DATE);
    int count = 0;

    this.reset(forecastGenerator);

    while (!itDate.isAfter(toDate)) {
      Forecast forecast =
          this.createForecast(
              forecastGenerator,
              forecastGenerator.getCompany(),
              forecastGenerator.getBankDetails(),
              forecastGenerator.getTypeSelect(),
              forecastGenerator.getAmount(),
              itDate,
              forecastGenerator.getForecastRecapLineType(),
              forecastGenerator.getComments());
      forecastRepo.save(forecast);
      itDate = fromDate.plusMonths(++count * forecastGenerator.getPeriodicitySelect());
    }
  }

  public Forecast createForecast(
      ForecastGenerator forecastGenerator,
      Company company,
      BankDetails bankDetails,
      int typeSelect,
      BigDecimal amount,
      LocalDate estimatedDate,
      ForecastRecapLineType lineType,
      String comments) {

    Forecast forecast = new Forecast();
    forecast.setForecastGenerator(forecastGenerator);
    forecast.setCompany(company);
    forecast.setBankDetails(bankDetails);
    forecast.setAmount(amount);
    forecast.setEstimatedDate(estimatedDate);
    forecast.setForecastRecapLineType(lineType);
    forecast.setComments(comments);
    forecast.setTypeSelect(typeSelect);

    return forecast;
  }

  @Transactional
  public void reset(ForecastGenerator forecastGenerator) {
    forecastRepo
        .all()
        .filter("self.forecastGenerator = ? AND self.realizationDate IS NULL", forecastGenerator)
        .remove();
  }
}
