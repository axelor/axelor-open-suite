package com.axelor.apps.budget.service.purchaseorder;

import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.businessproject.service.PurchaseOrderLineServiceProjectImpl;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;

public class PurchaseOrderLineGroupBudgetServiceImpl extends PurchaseOrderLineServiceProjectImpl {

  protected BudgetToolsService budgetToolsService;
  protected AppBudgetService appBudgetService;

  @Inject
  public PurchaseOrderLineGroupBudgetServiceImpl(
      AnalyticMoveLineService analyticMoveLineService,
      UnitConversionService unitConversionService,
      AppAccountService appAccountService,
      AccountConfigService accountConfigService,
      AnalyticLineModelService analyticLineModelService,
      PurchaseOrderLineRepository purchaseOrderLineRepo,
      BudgetToolsService budgetToolsService,
      AppBudgetService appBudgetService) {
    super(
        analyticMoveLineService,
        unitConversionService,
        appAccountService,
        accountConfigService,
        analyticLineModelService,
        purchaseOrderLineRepo);
    this.budgetToolsService = budgetToolsService;
    this.appBudgetService = appBudgetService;
  }

  @Override
  public Map<String, BigDecimal> compute(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder) throws AxelorException {

    Map<String, BigDecimal> map = super.compute(purchaseOrderLine, purchaseOrder);

    if (appBudgetService.isApp("budget")) {
      map.put(
          "budgetRemainingAmountToAllocate",
          budgetToolsService.getBudgetRemainingAmountToAllocate(
              purchaseOrderLine.getBudgetDistributionList(),
              purchaseOrderLine.getCompanyExTaxTotal()));
    }
    return map;
  }
}
