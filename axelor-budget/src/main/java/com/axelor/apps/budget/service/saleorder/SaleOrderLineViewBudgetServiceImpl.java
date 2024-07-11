package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineViewBudgetServiceImpl implements SaleOrderLineViewBudgetService {

  protected AppBudgetService appBudgetService;
  protected BudgetToolsService budgetToolsService;

  public static final String HIDDEN_ATTR = "hidden";
  public static final String TITLE_ATTR = "title";
  public static final String SCALE_ATTR = "scale";
  public static final String SELECTION_IN_ATTR = "selection-in";
  public static final String READONLY_ATTR = "readonly";

  @Inject
  public SaleOrderLineViewBudgetServiceImpl(
      AppBudgetService appBudgetService, BudgetToolsService budgetToolsService) {
    this.appBudgetService = appBudgetService;
    this.budgetToolsService = budgetToolsService;
  }

  @Override
  public Map<String, Map<String, Object>> getOnNewAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();

    if (appBudgetService.isApp("budget")) {
      attrs.putAll(manageBudgetKeyRoles(saleOrder));
    }

    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();

    if (appBudgetService.isApp("budget")) {
      attrs.putAll(manageBudgetKeyRoles(saleOrder));
    }

    return attrs;
  }

  protected Map<String, Map<String, Object>> manageBudgetKeyRoles(SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    Boolean isReadonly =
        !budgetToolsService.checkBudgetKeyAndRole(saleOrder.getCompany(), AuthUtils.getUser())
            || saleOrder.getStatusSelect() >= SaleOrderRepository.STATUS_ORDER_CONFIRMED;
    attrs.put("budgetDistributionPanel", Map.of(READONLY_ATTR, isReadonly));
    attrs.put("budget", Map.of(READONLY_ATTR, isReadonly));

    return attrs;
  }
}
