/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.cash.management.service;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.cash.management.db.Forecast;
import com.axelor.apps.cash.management.db.ForecastGenerator;
import com.axelor.apps.cash.management.db.ForecastReason;
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
              forecastGenerator.getForecastReason(),
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
      ForecastReason reason,
      String comments) {

    Forecast forecast = new Forecast();
    forecast.setForecastGenerator(forecastGenerator);
    forecast.setCompany(company);
    forecast.setBankDetails(bankDetails);
    forecast.setAmount(amount);
    forecast.setEstimatedDate(estimatedDate);
    forecast.setForecastReason(reason);
    forecast.setComments(comments);

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
