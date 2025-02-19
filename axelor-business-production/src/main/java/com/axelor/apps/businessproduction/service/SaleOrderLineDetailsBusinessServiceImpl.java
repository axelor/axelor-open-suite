package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.service.SaleOrderLineDetailsServiceImpl;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.MarginComputeService;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineProductService;
import com.google.inject.Inject;

public class SaleOrderLineDetailsBusinessServiceImpl extends SaleOrderLineDetailsServiceImpl {

  @Inject
  public SaleOrderLineDetailsBusinessServiceImpl(
      ProductCompanyService productCompanyService,
      AppSaleService appSaleService,
      SaleOrderLineProductService saleOrderLineProductService,
      MarginComputeService marginComputeService) {
    super(productCompanyService, appSaleService, saleOrderLineProductService, marginComputeService);
  }

  @Override
  public SaleOrder getParentSaleOrder(SaleOrderLineDetails saleOrderLineDetails) {
    SaleOrderLine saleOrderLine =
        saleOrderLineDetails.getSaleOrderLine() != null
            ? saleOrderLineDetails.getSaleOrderLine()
            : saleOrderLineDetails.getProjectSaleOrderLine();
    return saleOrderLine.getSaleOrder();
  }
}
