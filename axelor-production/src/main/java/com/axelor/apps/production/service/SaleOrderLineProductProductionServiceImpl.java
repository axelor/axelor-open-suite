package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.InternationalService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineComplementaryProductService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineDiscountService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLinePriceService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLinePricingService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineTaxService;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.apps.supplychain.service.SaleOrderLineProductSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineProductProductionServiceImpl
    extends SaleOrderLineProductSupplychainServiceImpl
    implements SaleOrderLineProductProductionService {

  protected AppProductionService appProductionService;

  @Inject
  public SaleOrderLineProductProductionServiceImpl(
      AppSaleService appSaleService,
      AppBaseService appBaseService,
      SaleOrderLineComplementaryProductService saleOrderLineComplementaryProductService,
      InternationalService internationalService,
      TaxService taxService,
      AccountManagementService accountManagementService,
      SaleOrderLinePricingService saleOrderLinePricingService,
      SaleOrderLineDiscountService saleOrderLineDiscountService,
      SaleOrderLinePriceService saleOrderLinePriceService,
      SaleOrderLineTaxService saleOrderLineTaxService,
      BlockingService blockingService,
      AnalyticLineModelService analyticLineModelService,
      AppSupplychainService appSupplychainService,
      AppProductionService appProductionService) {
    super(
        appSaleService,
        appBaseService,
        saleOrderLineComplementaryProductService,
        internationalService,
        taxService,
        accountManagementService,
        saleOrderLinePricingService,
        saleOrderLineDiscountService,
        saleOrderLinePriceService,
        saleOrderLineTaxService,
        blockingService,
        analyticLineModelService,
        appSupplychainService);
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
