package com.axelor.apps.budget.service.saleorderline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineViewBudgetServiceImpl implements SaleOrderLineViewBudgetService {

  protected BudgetToolsService budgetToolsService;

  @Inject
  public SaleOrderLineViewBudgetServiceImpl(BudgetToolsService budgetToolsService) {
    this.budgetToolsService = budgetToolsService;
  }

  @Override
  public Map<String, Map<String, Object>> checkBudget(SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    if (saleOrder != null && saleOrder.getCompany() != null) {
      attrs.put(
          "budgetDistributionPanel",
          Map.of(
              "readonly",
              !budgetToolsService.checkBudgetKeyAndRole(saleOrder.getCompany(), AuthUtils.getUser())
                  || saleOrder.getStatusSelect() >= SaleOrderRepository.STATUS_ORDER_CONFIRMED));

      attrs.put(
          "budget",
          Map.of(
              "readonly",
              !budgetToolsService.checkBudgetKeyAndRole(saleOrder.getCompany(), AuthUtils.getUser())
                  || saleOrder.getStatusSelect() >= SaleOrderRepository.STATUS_ORDER_CONFIRMED));
    }
    return attrs;
  }
}
