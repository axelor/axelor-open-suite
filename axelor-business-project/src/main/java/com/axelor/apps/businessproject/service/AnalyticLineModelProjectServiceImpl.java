package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.analytic.AnalyticToolService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.businessproject.model.AnalyticLineProjectModel;
import com.axelor.apps.purchase.service.config.PurchaseConfigService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelServiceImpl;
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
      PurchaseConfigService purchaseConfigService) {
    super(
        appBaseService,
        appAccountService,
        analyticMoveLineService,
        accountManagementAccountService,
        analyticToolService,
        saleConfigService,
        purchaseConfigService);
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

    if (analyticLineProjectModel.getProject() != null) {
      List<AnalyticMoveLine> analyticMoveLineList =
          analyticLineProjectModel.getAnalyticMoveLineList();

      analyticMoveLineList.forEach(
          analyticMoveLine -> analyticMoveLine.setProject(analyticLineProjectModel.getProject()));
      analyticLineProjectModel.setAnalyticMoveLineList(analyticMoveLineList);
    }

    return analyticLineProjectModel;
  }
}
