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
package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticJournal;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class AnalyticLineServiceImpl implements AnalyticLineService {

  private static final int RETURN_SCALE = 2;
  protected AccountConfigService accountConfigService;
  protected AppBaseService appBaseService;

  @Inject
  public AnalyticLineServiceImpl(
      AccountConfigService accountConfigService, AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
    this.accountConfigService = accountConfigService;
  }

  @Override
  public AnalyticJournal getAnalyticJournal(AnalyticLine analyticLine) throws AxelorException {
    if (analyticLine.getAccount() != null && analyticLine.getAccount().getCompany() != null) {
      return accountConfigService
          .getAccountConfig(analyticLine.getAccount().getCompany())
          .getAnalyticJournal();
    }
    return null;
  }

  @Override
  public LocalDate getDate(AnalyticLine analyticLine) {
    if (analyticLine instanceof MoveLine) {
      MoveLine line = (MoveLine) analyticLine;
      if (line.getDate() != null) {
        return line.getDate();
      }
    }
    if (analyticLine.getAccount() != null && analyticLine.getAccount().getCompany() != null) {
      return appBaseService.getTodayDate(analyticLine.getAccount().getCompany());
    }
    return appBaseService.getTodayDate(null);
  }

  @Override
  public BigDecimal getAnalyticAmountFromParent(
      AnalyticLine parent, AnalyticMoveLine analyticMoveLine) {

    if (parent.getLineAmount().signum() > 0) {
      return analyticMoveLine
          .getPercentage()
          .multiply(parent.getLineAmount())
          .divide(new BigDecimal(100), RETURN_SCALE, RoundingMode.HALF_UP);
    }
    return BigDecimal.ZERO;
  }
}
