package com.axelor.apps.production.service;

import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.InternationalService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.pricing.PricingService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.production.service.app.AppProductionService;
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
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineProductionServiceImpl extends SaleOrderLineServiceSupplyChainImpl
    implements SaleOrderLineProductionService {

  protected AppProductionService appProductionService;

  @Inject
  public SaleOrderLineProductionServiceImpl(
      CurrencyService currencyService,
      PriceListService priceListService,
      ProductMultipleQtyService productMultipleQtyService,
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      AccountManagementService accountManagementService,
      SaleOrderLineRepository saleOrderLineRepo,
      SaleOrderService saleOrderService,
      PricingService pricingService,
      TaxService taxService,
      SaleOrderMarginService saleOrderMarginService,
      CurrencyScaleService currencyScaleService,
      InternationalService internationalService,
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      AppSupplychainService appSupplychainService,
      AccountConfigService accountConfigService,
      InvoiceLineRepository invoiceLineRepository,
      SaleInvoicingStateService saleInvoicingStateService,
      AnalyticLineModelService analyticLineModelService,
      BlockingService blockingService,
      AppProductionService appProductionService) {
    super(
        currencyService,
        priceListService,
        productMultipleQtyService,
        appBaseService,
        appSaleService,
        accountManagementService,
        saleOrderLineRepo,
        saleOrderService,
        pricingService,
        taxService,
        saleOrderMarginService,
        currencyScaleService,
        internationalService,
        appAccountService,
        analyticMoveLineService,
        appSupplychainService,
        accountConfigService,
        invoiceLineRepository,
        saleInvoicingStateService,
        analyticLineModelService,
        blockingService);
    this.appProductionService = appProductionService;
  }

  @Override
  public Map<String, Object> computeProductInformation(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> saleOrderLineMap =
        super.computeProductInformation(saleOrderLine, saleOrder);
    saleOrderLineMap.putAll(setBillOfMaterial(saleOrderLine));

    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> setBillOfMaterial(SaleOrderLine saleOrderLine) {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    if (appProductionService.isApp("production")) {
      Product product = saleOrderLine.getProduct();

      if (product != null) {
        if (product.getDefaultBillOfMaterial() != null) {
          saleOrderLine.setBillOfMaterial(product.getDefaultBillOfMaterial());
        } else if (product.getParentProduct() != null) {
          saleOrderLine.setBillOfMaterial(product.getParentProduct().getDefaultBillOfMaterial());
        }
        saleOrderLineMap.put("billOfMaterial", saleOrderLine.getBillOfMaterial());
      }
    }
    return saleOrderLineMap;
  }
}
