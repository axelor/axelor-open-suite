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
package com.axelor.apps.businessproject.service.analytic;

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.businessproject.model.AnalyticLineProjectModel;
import com.axelor.apps.businessproject.service.config.BusinessProjectConfigService;
import com.axelor.apps.purchase.service.config.PurchaseConfigService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelServiceImpl;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class AnalyticLineModelProjectServiceImpl extends AnalyticLineModelServiceImpl
    implements AnalyticLineModelProjectService {

  protected BusinessProjectConfigService businessProjectConfigService;
  protected ProjectAnalyticMoveLineService projectAnalyticMoveLineService;

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
      BusinessProjectConfigService businessProjectConfigService,
      ProjectAnalyticMoveLineService projectAnalyticMoveLineService) {
    super(
        appBaseService,
        appAccountService,
        analyticMoveLineService,
        accountManagementAccountService,
        analyticToolService,
        saleConfigService,
        purchaseConfigService,
        currencyScaleService);
    this.businessProjectConfigService = businessProjectConfigService;
    this.projectAnalyticMoveLineService = projectAnalyticMoveLineService;
  }

  @Override
  public boolean computeAnalyticMoveLineList(
      AnalyticLineProjectModel analyticLineProjectModel, Company company) throws AxelorException {
    if (!analyticToolService.isManageAnalytic(company) || analyticLineProjectModel == null) {
      return false;
    }

    if (analyticLineProjectModel.getAnalyticMoveLineList() == null) {
      analyticLineProjectModel.setAnalyticMoveLineList(new ArrayList<>());
    } else {
      analyticLineProjectModel.getAnalyticMoveLineList().clear();
    }

    for (AnalyticAccount axisAnalyticAccount :
        this.getAxisAnalyticAccountList(analyticLineProjectModel)) {
      AnalyticMoveLine analyticMoveLine =
          this.computeAnalyticMoveLine(analyticLineProjectModel, company, axisAnalyticAccount);

      analyticLineProjectModel.addAnalyticMoveLineListItem(analyticMoveLine);
    }

    return true;
  }

  protected AnalyticMoveLine computeAnalyticMoveLine(
      AnalyticLineProjectModel analyticLineProjectModel,
      Company company,
      AnalyticAccount analyticAccount)
      throws AxelorException {

    AnalyticMoveLine analyticMoveLine =
        super.computeAnalyticMoveLine(analyticLineProjectModel, company, analyticAccount);

    if (analyticLineProjectModel.getProject() != null) {
      analyticMoveLine.setProject(analyticLineProjectModel.getProject());
    }

    return analyticMoveLine;
  }

  @Override
  public AnalyticLineProjectModel getAndComputeAnalyticDistribution(
      AnalyticLineProjectModel analyticLineProjectModel) throws AxelorException {
    AnalyticDistributionTemplate analyticDistributionTemplate =
        projectAnalyticMoveLineService.getAnalyticDistributionTemplate(
            analyticLineProjectModel.getProject(),
            analyticLineProjectModel.getPartner(),
            analyticLineProjectModel.getProduct(),
            analyticLineProjectModel.getCompany(),
            analyticLineProjectModel.getTradingName(),
            analyticLineProjectModel.getAccount(),
            analyticLineProjectModel.getIsPurchase());

    analyticLineProjectModel.setAnalyticDistributionTemplate(analyticDistributionTemplate);

    if (analyticLineProjectModel.getAnalyticMoveLineList() != null) {
      analyticLineProjectModel.getAnalyticMoveLineList().clear();
    }

    this.computeAnalyticDistribution(analyticLineProjectModel);

    analyticLineProjectModel.copyToModel();

    return analyticLineProjectModel;
  }

  @Override
  public AnalyticLineModel createAnalyticDistributionWithTemplate(
      AnalyticLineProjectModel analyticLineProjectModel) throws AxelorException {
    super.createAnalyticDistributionWithTemplate(analyticLineProjectModel);

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

  public boolean analyticDistributionTemplateRequired(AnalyticLineModel analyticLineModel)
      throws AxelorException {
    return analyticToolService.isManageAnalytic(analyticLineModel.getCompany())
        && businessProjectConfigService
            .getBusinessProjectConfig(analyticLineModel.getCompany())
            .getIsAnalyticDistributionRequired()
        && ObjectUtils.isEmpty(analyticLineModel.getAnalyticMoveLineList());
  }

  protected AnalyticLineProjectModel getAnalyticLineProjectModel(
      AnalyticLineModel analyticLineModel) throws AxelorException {
    return analyticLineModel.getExtension(AnalyticLineProjectModel.class);
  }
}
