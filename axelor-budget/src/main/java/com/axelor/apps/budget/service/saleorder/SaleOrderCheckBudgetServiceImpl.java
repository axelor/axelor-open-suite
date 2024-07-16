package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.budget.web.tool.BudgetControllerTool;
import com.axelor.apps.sale.db.SaleOrder;
import com.google.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderCheckBudgetServiceImpl implements SaleOrderCheckBudgetService {

  protected SaleOrderBudgetService saleOrderBudgetService;

  @Inject
  public SaleOrderCheckBudgetServiceImpl(SaleOrderBudgetService saleOrderBudgetService) {
    this.saleOrderBudgetService = saleOrderBudgetService;
  }

  @Override
  public String checkBudgetBeforeFinalize(SaleOrder saleOrder) {
    if (saleOrder != null && !CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
      if (saleOrderBudgetService.isBudgetInLines(saleOrder)) {
        String budgetExceedAlert = saleOrderBudgetService.getBudgetExceedAlert(saleOrder);
        return BudgetControllerTool.getVerifyBudgetExceedAlert(budgetExceedAlert);
      } else {
        return BudgetControllerTool.getVerifyMissingBudgetAlert();
      }
    }
    return "";
  }
}
