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

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.service.ContractLineServiceImpl;
import com.axelor.apps.project.db.Project;
import com.google.inject.Inject;
import java.util.List;

public class ContractLineServiceProjectImpl extends ContractLineServiceImpl {

  @Inject
  public ContractLineServiceProjectImpl(
      AppBaseService appBaseService,
      AccountManagementService accountManagementService,
      CurrencyService currencyService,
      ProductCompanyService productCompanyService) {
    super(appBaseService, accountManagementService, currencyService, productCompanyService);
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
}
