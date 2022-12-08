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
package com.axelor.apps.account.service.analytic;

import java.math.BigDecimal;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;

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

  void computeAnalyticDistribution(Move move, MoveLine moveLine, BigDecimal amount) throws AxelorException;
}
