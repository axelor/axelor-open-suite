package com.axelor.apps.supplychain.service.saleorderline;

import com.axelor.apps.account.service.analytic.AnalyticGroupService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineAnalyticServiceImpl implements SaleOrderLineAnalyticService {

  protected AnalyticGroupService analyticGroupService;

  @Inject
  public SaleOrderLineAnalyticServiceImpl(AnalyticGroupService analyticGroupService) {
    this.analyticGroupService = analyticGroupService;
  }

  @Override
  public Map<String, Object> printAnalyticAccounts(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    AnalyticLineModel analyticLineModel = new AnalyticLineModel(saleOrderLine, saleOrder);
    return analyticGroupService.getAnalyticAccountValueMap(
        analyticLineModel, saleOrder.getCompany());
  }
}
