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
package com.axelor.apps.supplychain.service.analytic;

import com.axelor.apps.account.db.AnalyticJournal;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.Optional;

public class AnalyticMoveLineRecordServiceImpl implements AnalyticMoveLineRecordService {

  protected AccountConfigService accountConfigService;
  protected AppBaseService appBaseService;

  @Inject
  public AnalyticMoveLineRecordServiceImpl(
      AccountConfigService accountConfigService, AppBaseService appBaseService) {
    this.accountConfigService = accountConfigService;
    this.appBaseService = appBaseService;
  }

  @Override
  public void onNew(AnalyticLineModel analyticLineModel, AnalyticMoveLine analyticMoveLine)
      throws AxelorException {
    analyticMoveLine.setAnalyticJournal(getAnalyticJournal(analyticLineModel));
    analyticMoveLine.setDate(getDate(analyticLineModel));
    analyticMoveLine.setCompanyCurrency(
        Optional.of(analyticLineModel)
            .map(AnalyticLineModel::getCompany)
            .map(Company::getCurrency)
            .orElse(null));
  }

  protected AnalyticJournal getAnalyticJournal(AnalyticLineModel analyticLineModel)
      throws AxelorException {
    if (analyticLineModel.getCompany() != null) {
      return accountConfigService
          .getAccountConfig(analyticLineModel.getCompany())
          .getAnalyticJournal();
    }
    return null;
  }

  protected LocalDate getDate(AnalyticLineModel analyticLineModel) {
    if (analyticLineModel.getCompany() != null) {
      return appBaseService.getTodayDate(analyticLineModel.getCompany());
    }
    return appBaseService.getTodayDate(null);
  }
}
