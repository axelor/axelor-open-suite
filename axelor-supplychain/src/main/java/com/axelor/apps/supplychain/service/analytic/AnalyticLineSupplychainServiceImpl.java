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

import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.model.AnalyticLineModel;
import com.axelor.apps.account.service.AccountService;
import com.axelor.apps.account.service.analytic.AnalyticLineServiceImpl;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrderLine;
import jakarta.inject.Inject;

public class AnalyticLineSupplychainServiceImpl extends AnalyticLineServiceImpl {

  @Inject
  public AnalyticLineSupplychainServiceImpl(
      AccountConfigService accountConfigService,
      AppBaseService appBaseService,
      AnalyticToolService analyticToolService,
      AnalyticAccountRepository analyticAccountRepository,
      AccountService accountService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      CurrencyScaleService currencyScaleService) {
    super(
        accountConfigService,
        appBaseService,
        analyticToolService,
        analyticAccountRepository,
        accountService,
        moveLineComputeAnalyticService,
        currencyScaleService);
  }

  /**
   * On a sale order line, the isRequired flag of the analytic axis by company applies whatever the
   * analytic distribution type of the company, and not only when the distribution is free. Such a
   * line carries no accounting account of its own, so the requirement held by the account is not a
   * relevant fallback there.
   */
  @Override
  public boolean isAxisRequired(AnalyticLineModel analyticLineModel, int position)
      throws AxelorException {
    if (!(analyticLineModel.getAnalyticLine() instanceof SaleOrderLine)
        || analyticToolService.isFreeAnalyticDistribution(analyticLineModel.getCompany())) {
      return super.isAxisRequired(analyticLineModel, position);
    }

    if (!this.isAxisRequirementApplicable(analyticLineModel, position)) {
      return false;
    }

    return this.isAxisRequiredByCompanyConfig(analyticLineModel.getCompany(), position)
        || this.isAxisRequiredByAccount(analyticLineModel);
  }
}
