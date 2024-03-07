package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.pricing.PricingService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.apps.supplychain.service.SaleInvoicingStateService;
import com.axelor.apps.supplychain.service.SaleOrderLineServiceSupplyChainImpl;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;

public class SaleOrderLineGroupBudgetServiceImpl extends SaleOrderLineServiceSupplyChainImpl {

  protected BudgetToolsService budgetToolsService;
  protected AppBudgetService appBudgetService;

  @Inject
  public SaleOrderLineGroupBudgetServiceImpl(
      CurrencyService currencyService,
      PriceListService priceListService,
      ProductMultipleQtyService productMultipleQtyService,
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      AccountManagementService accountManagementService,
      SaleOrderLineRepository saleOrderLineRepo,
      SaleOrderService saleOrderService,
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      AppSupplychainService appSupplychainService,
      AccountConfigService accountConfigService,
      PricingService pricingService,
      TaxService taxService,
      SaleOrderMarginService saleOrderMarginService,
      InvoiceLineRepository invoiceLineRepository,
      SaleInvoicingStateService saleInvoicingStateService,
      AnalyticLineModelService analyticLineModelService,
      CurrencyScaleService currencyScaleService,
      BudgetToolsService budgetToolsService,
      AppBudgetService appBudgetService) {
    super(
        currencyService,
        priceListService,
        productMultipleQtyService,
        appBaseService,
        appSaleService,
        accountManagementService,
        saleOrderLineRepo,
        saleOrderService,
        appAccountService,
        analyticMoveLineService,
        appSupplychainService,
        accountConfigService,
        pricingService,
        taxService,
        saleOrderMarginService,
        invoiceLineRepository,
        saleInvoicingStateService,
        analyticLineModelService,
        currencyScaleService);
    this.budgetToolsService = budgetToolsService;
    this.appBudgetService = appBudgetService;
  }

  @Override
  public Map<String, BigDecimal> computeValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {

    Map<String, BigDecimal> map = super.computeValues(saleOrder, saleOrderLine);

    if (appBudgetService.isApp("budget")) {
      saleOrderLine.setBudgetRemainingAmountToAllocate(
          budgetToolsService.getBudgetRemainingAmountToAllocate(
              saleOrderLine.getBudgetDistributionList(), saleOrderLine.getCompanyExTaxTotal()));
      map.put(
          "budgetRemainingAmountToAllocate", saleOrderLine.getBudgetRemainingAmountToAllocate());
    }

    return map;
  }
}
