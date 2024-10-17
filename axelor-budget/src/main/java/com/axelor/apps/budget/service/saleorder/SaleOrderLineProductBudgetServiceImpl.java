package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.saleorderline.SaleOrderLineBudgetService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineProductBudgetServiceImpl implements SaleOrderLineProductBudgetService {

  protected SaleOrderLineBudgetService saleOrderLineBudgetService;

  @Inject
  public SaleOrderLineProductBudgetServiceImpl(
      SaleOrderLineBudgetService saleOrderLineBudgetService) {
    this.saleOrderLineBudgetService = saleOrderLineBudgetService;
  }

  @Override
  public Map<String, Object> computeProductInformationBudget(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.putAll(saleOrderLineBudgetService.setProductAccount(saleOrder, saleOrderLine));
    saleOrderLineMap.putAll(saleOrderLineBudgetService.resetBudget(saleOrderLine));
    return saleOrderLineMap;
  }
}
