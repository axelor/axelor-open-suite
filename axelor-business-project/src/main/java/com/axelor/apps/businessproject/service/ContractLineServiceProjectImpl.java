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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.repo.AccountAnalyticRulesRepository;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.contract.service.ContractLineServiceImpl;
import com.axelor.utils.service.ListToolService;
import com.google.inject.Inject;

public class ContractLineServiceProjectImpl extends ContractLineServiceImpl {

  @Inject
  public ContractLineServiceProjectImpl(
      AppBaseService appBaseService,
      AppAccountService appAccountService,
      AccountManagementService accountManagementService,
      AccountConfigService accountConfigService,
      CurrencyService currencyService,
      ProductCompanyService productCompanyService,
      AnalyticMoveLineService analyticMoveLineService,
      AnalyticToolService analyticToolService,
      ListToolService listToolService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      AnalyticAccountRepository analyticAccountRepo,
      AccountAnalyticRulesRepository accountAnalyticRulesRepo) {
    super(
        appBaseService,
        appAccountService,
        accountManagementService,
        accountConfigService,
        currencyService,
        productCompanyService,
        analyticMoveLineService,
        analyticToolService,
        listToolService,
        moveLineComputeAnalyticService,
        analyticAccountRepo,
        accountAnalyticRulesRepo);
  }
}
