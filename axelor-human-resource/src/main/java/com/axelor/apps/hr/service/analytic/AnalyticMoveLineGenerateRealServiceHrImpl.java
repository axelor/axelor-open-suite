/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.analytic;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineGenerateRealServiceImpl;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.google.inject.Inject;

public class AnalyticMoveLineGenerateRealServiceHrImpl
    extends AnalyticMoveLineGenerateRealServiceImpl {

  @Inject
  public AnalyticMoveLineGenerateRealServiceHrImpl(
      AnalyticMoveLineRepository analyticMoveLineRepository,
      AnalyticMoveLineService analyticMoveLineService,
      AccountConfigService accountConfigService,
      AppAccountService appAccountService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      AccountManagementAccountService accountManagementAccountService) {
    super(
        analyticMoveLineRepository,
        analyticMoveLineService,
        accountConfigService,
        appAccountService,
        moveLineComputeAnalyticService,
        accountManagementAccountService);
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
