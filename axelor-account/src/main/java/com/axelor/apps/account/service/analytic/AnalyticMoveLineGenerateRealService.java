/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;
import java.util.List;

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

  /**
   * Generate real analytic move lines from a list of forecast move lines, add them to the given
   * move line and reconcile the per-line rounding so that, for each analytic axis, the sum of the
   * generated amounts equals the move line amount (debit + credit).
   *
   * @param forecastAnalyticMoveLineList the forecast analytic move lines to copy.
   * @param moveLine the move line that will be linked to and receive the created analytic move
   *     lines.
   */
  void createFromForecastList(
      List<AnalyticMoveLine> forecastAnalyticMoveLineList, MoveLine moveLine);

  void computeAnalyticDistribution(Move move, MoveLine moveLine, BigDecimal amount)
      throws AxelorException;
}
