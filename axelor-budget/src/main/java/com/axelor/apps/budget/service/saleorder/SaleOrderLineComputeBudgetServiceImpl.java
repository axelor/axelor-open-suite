package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.apps.supplychain.service.SaleOrderLineComputeSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderLineServiceSupplyChain;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineComputeBudgetServiceImpl
    extends SaleOrderLineComputeSupplychainServiceImpl {

  protected BudgetToolsService budgetToolsService;
  protected AppBudgetService appBudgetService;

  @Inject
  public SaleOrderLineComputeBudgetServiceImpl(
      TaxService taxService,
      CurrencyScaleService currencyScaleService,
      ProductCompanyService productCompanyService,
      SaleOrderMarginService saleOrderMarginService,
      CurrencyService currencyService,
      PriceListService priceListService,
      SaleOrderLineService saleOrderLineService,
      AppBaseService appBaseService,
      AppSupplychainService appSupplychainService,
      AppAccountService appAccountService,
      AnalyticLineModelService analyticLineModelService,
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain,
      BudgetToolsService budgetToolsService,
      AppBudgetService appBudgetService) {
    super(
        taxService,
        currencyScaleService,
        productCompanyService,
        saleOrderMarginService,
        currencyService,
        priceListService,
        saleOrderLineService,
        appBaseService,
        appSupplychainService,
        appAccountService,
        analyticLineModelService,
        saleOrderLineServiceSupplyChain);
    this.budgetToolsService = budgetToolsService;
    this.appBudgetService = appBudgetService;
  }

  @Override
  public Map<String, Object> computeValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = super.computeValues(saleOrder, saleOrderLine);

    if (appBudgetService.isApp("budget")) {
      saleOrderLine.setBudgetRemainingAmountToAllocate(
          budgetToolsService.getBudgetRemainingAmountToAllocate(
              saleOrderLine.getBudgetDistributionList(), saleOrderLine.getCompanyExTaxTotal()));
      saleOrderLineMap.put(
          "budgetRemainingAmountToAllocate", saleOrderLine.getBudgetRemainingAmountToAllocate());
    }
    return saleOrderLineMap;
  }
}
