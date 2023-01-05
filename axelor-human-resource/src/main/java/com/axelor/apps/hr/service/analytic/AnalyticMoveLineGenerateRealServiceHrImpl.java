/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.analytic;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineGenerateRealServiceImpl;
import com.google.inject.Inject;

public class AnalyticMoveLineGenerateRealServiceHrImpl
    extends AnalyticMoveLineGenerateRealServiceImpl {

  @Inject
  public AnalyticMoveLineGenerateRealServiceHrImpl(
      AnalyticMoveLineRepository analyticMoveLineRepository,
      AnalyticMoveLineService analyticMoveLineService) {
    super(analyticMoveLineRepository, analyticMoveLineService);
  }

  @Override
  public AnalyticMoveLine createFromForecast(
      AnalyticMoveLine forecastAnalyticMoveLine, MoveLine moveLine) {
    AnalyticMoveLine analyticMoveLine =
        super.createFromForecast(forecastAnalyticMoveLine, moveLine);
    analyticMoveLine.setExpenseLine(null);
    return analyticMoveLine;
  }
}
