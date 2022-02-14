/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
