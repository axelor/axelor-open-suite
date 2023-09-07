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

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.businessproject.model.AnalyticLineProjectModel;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.db.repo.ContractVersionRepository;
import com.axelor.apps.contract.service.ContractLineServiceImpl;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.google.inject.Inject;

public class ContractLineServiceProjectImpl extends ContractLineServiceImpl {

  @Inject
  public ContractLineServiceProjectImpl(
      AppBaseService appBaseService,
      AccountManagementService accountManagementService,
      CurrencyService currencyService,
      ProductCompanyService productCompanyService,
      PriceListService priceListService,
      ContractVersionRepository contractVersionRepo,
      DurationService durationService,
      AnalyticLineModelService analyticLineModelService,
      AppAccountService appAccountService) {
    super(
        appBaseService,
        accountManagementService,
        currencyService,
        productCompanyService,
        priceListService,
        contractVersionRepo,
        durationService,
        analyticLineModelService,
        appAccountService);
  }

  @Override
  public void computeAnalytic(Contract contract, ContractLine contractLine) throws AxelorException {
    if (appAccountService.isApp("business-project")) {
      AnalyticLineModel analyticLineModel =
          new AnalyticLineProjectModel(contractLine, null, contract);
      analyticLineModelService.getAndComputeAnalyticDistribution(analyticLineModel);
    }
  }
}
