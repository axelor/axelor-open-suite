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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.service.ContractLineServiceImpl;
import com.axelor.apps.project.db.Project;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.util.List;

public class ContractLineServiceProjectImpl extends ContractLineServiceImpl {

  @Inject
  public ContractLineServiceProjectImpl(
      AppBaseService appBaseService,
      AccountManagementService accountManagementService,
      CurrencyService currencyService,
      ProductCompanyService productCompanyService,
      AnalyticMoveLineService analyticMoveLineService,
      AccountConfigService accountConfigService,
      AppAccountService appAccountService) {
    super(
        appBaseService,
        accountManagementService,
        currencyService,
        productCompanyService,
        analyticMoveLineService,
        accountConfigService,
        appAccountService);
  }

  @Override
  public ContractLine createAnalyticDistributionWithTemplate(
      ContractLine contractLine, Contract contract) {
    contractLine = super.createAnalyticDistributionWithTemplate(contractLine, contract);

    Project project = contract.getProject();

    if (project != null) {
      List<AnalyticMoveLine> analyticMoveLineList = contractLine.getAnalyticMoveLineList();

      analyticMoveLineList.forEach(analyticMoveLine -> analyticMoveLine.setProject(project));
      contractLine.setAnalyticMoveLineList(analyticMoveLineList);
    }
    return contractLine;
  }

  @Override
  public AnalyticMoveLine computeAnalyticMoveLine(
      ContractLine contractLine,
      Contract contract,
      Company company,
      AnalyticAccount analyticAccount)
      throws AxelorException {

    AnalyticMoveLine analyticMoveLine =
        super.computeAnalyticMoveLine(contractLine, contract, company, analyticAccount);
    Project project = contract.getProject();

    if (project != null) {
      analyticMoveLine.setProject(project);
    }
    return analyticMoveLine;
  }
}
