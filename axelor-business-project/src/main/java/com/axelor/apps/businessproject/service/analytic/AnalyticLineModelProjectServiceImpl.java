/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.service.analytic;

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.analytic.AnalyticAxisService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.businessproject.model.AnalyticLineProjectModel;
import com.axelor.apps.purchase.service.config.PurchaseConfigService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelServiceImpl;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.List;

public class AnalyticLineModelProjectServiceImpl extends AnalyticLineModelServiceImpl {

  @Inject
  public AnalyticLineModelProjectServiceImpl(
      AppBaseService appBaseService,
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      AccountManagementAccountService accountManagementAccountService,
      AnalyticToolService analyticToolService,
      SaleConfigService saleConfigService,
      PurchaseConfigService purchaseConfigService,
      CurrencyScaleService currencyScaleService,
      AnalyticAxisService analyticAxisService) {
    super(
        appBaseService,
        appAccountService,
        analyticMoveLineService,
        accountManagementAccountService,
        analyticToolService,
        saleConfigService,
        purchaseConfigService,
        currencyScaleService,
        analyticAxisService);
  }

  @Override
  protected AnalyticMoveLine computeAnalyticMoveLine(
      AnalyticLineModel analyticLineModel, Company company, AnalyticAccount analyticAccount)
      throws AxelorException {
    AnalyticLineProjectModel analyticLineProjectModel =
        analyticLineModel.getExtension(AnalyticLineProjectModel.class);

    AnalyticMoveLine analyticMoveLine =
        super.computeAnalyticMoveLine(analyticLineProjectModel, company, analyticAccount);

    if (analyticLineProjectModel.getProject() != null) {
      analyticMoveLine.setProject(analyticLineProjectModel.getProject());
    }

    return analyticMoveLine;
  }

  @Override
  public AnalyticLineModel createAnalyticDistributionWithTemplate(
      AnalyticLineModel analyticLineModel) throws AxelorException {
    AnalyticLineProjectModel analyticLineProjectModel =
        analyticLineModel.getExtension(AnalyticLineProjectModel.class);

    super.createAnalyticDistributionWithTemplate(analyticLineModel);

    if (analyticLineProjectModel.getProject() != null
        && ObjectUtils.notEmpty(analyticLineProjectModel.getAnalyticMoveLineList())) {
      List<AnalyticMoveLine> analyticMoveLineList =
          analyticLineProjectModel.getAnalyticMoveLineList();

      analyticMoveLineList.forEach(
          analyticMoveLine -> analyticMoveLine.setProject(analyticLineProjectModel.getProject()));
      analyticLineProjectModel.setAnalyticMoveLineList(analyticMoveLineList);
    }

    return analyticLineProjectModel;
  }
}
