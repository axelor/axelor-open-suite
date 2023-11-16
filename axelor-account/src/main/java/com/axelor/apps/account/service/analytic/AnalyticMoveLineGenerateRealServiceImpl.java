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
package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class AnalyticMoveLineGenerateRealServiceImpl
    implements AnalyticMoveLineGenerateRealService {

  protected AnalyticMoveLineRepository analyticMoveLineRepository;
  protected AnalyticMoveLineService analyticMoveLineService;
  protected AccountConfigService accountConfigService;
  protected AppAccountService appAccountService;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected AccountManagementAccountService accountManagementAccountService;
  protected AppBaseService appBaseService;
  protected AccountingSituationService accountingSituationService;

  @Inject
  public AnalyticMoveLineGenerateRealServiceImpl(
      AnalyticMoveLineRepository analyticMoveLineRepository,
      AnalyticMoveLineService analyticMoveLineService,
      AccountConfigService accountConfigService,
      AppAccountService appAccountService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      AccountManagementAccountService accountManagementAccountService,
      AppBaseService appBaseService,
      AccountingSituationService accountingSituationService) {
    this.analyticMoveLineRepository = analyticMoveLineRepository;
    this.analyticMoveLineService = analyticMoveLineService;
    this.accountConfigService = accountConfigService;
    this.appAccountService = appAccountService;
    this.accountManagementAccountService = accountManagementAccountService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.appBaseService = appBaseService;
    this.accountingSituationService = accountingSituationService;
  }

  @Override
  public AnalyticMoveLine createFromForecast(
      AnalyticMoveLine forecastAnalyticMoveLine, MoveLine moveLine) {
    AnalyticMoveLine analyticMoveLine =
        analyticMoveLineRepository.copy(forecastAnalyticMoveLine, false);
    analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_REAL_ACCOUNTING);
    analyticMoveLine.setInvoiceLine(null);
    analyticMoveLine.setAccount(moveLine.getAccount());
    analyticMoveLine.setAccountType(moveLine.getAccount().getAccountType());
    analyticMoveLineService.updateAnalyticMoveLine(
        analyticMoveLine, moveLine.getDebit().add(moveLine.getCredit()), moveLine.getDate());
    return analyticMoveLine;
  }

  @Override
  public void computeAnalyticDistribution(Move move, MoveLine moveLine, BigDecimal amount)
      throws AxelorException {
    if (move != null
        && move.getCompany() != null
        && moveLine != null
        && moveLine.getAccount() != null
        && moveLine.getAccount().getAnalyticDistributionAuthorized()) {
      TradingName tradingName = move.getTradingName();

      AnalyticDistributionTemplate analyticDistributionTemplate =
          analyticMoveLineService.getAnalyticDistributionTemplate(
              moveLine.getPartner(),
              null,
              move.getCompany(),
              tradingName,
              moveLine.getAccount(),
              false);

      if (analyticDistributionTemplate != null) {
        moveLine.setAnalyticDistributionTemplate(analyticDistributionTemplate);
        moveLineComputeAnalyticService.createAnalyticDistributionWithTemplate(moveLine);
      }
    }
  }
}
