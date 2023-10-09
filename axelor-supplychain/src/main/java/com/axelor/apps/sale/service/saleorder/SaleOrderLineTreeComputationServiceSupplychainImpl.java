package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderLineTreeComputationServiceSupplychainImpl
    extends SaleOrderLineTreeComputationServiceImpl {

  protected final AnalyticLineModelService analyticLineModelService;

  @Inject
  public SaleOrderLineTreeComputationServiceSupplychainImpl(
      AnalyticLineModelService analyticLineModelService) {
    this.analyticLineModelService = analyticLineModelService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void computePrices(SaleOrderLine saleOrderLine) throws AxelorException {
    super.computePrices(saleOrderLine);

    AnalyticLineModel analyticLineModel =
        new AnalyticLineModel(saleOrderLine, saleOrderLine.getSaleOrder());
    if (analyticLineModelService.productAccountManageAnalytic(analyticLineModel)) {
      analyticLineModelService.computeAnalyticDistribution(analyticLineModel);
    }
  }
}
