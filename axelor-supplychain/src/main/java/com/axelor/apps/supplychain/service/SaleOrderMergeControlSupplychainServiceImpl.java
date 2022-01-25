package com.axelor.apps.supplychain.service;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergeControlServiceImpl;
import com.axelor.apps.sale.service.saleorder.model.SaleOrderMergeObject;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderMergeControlSupplychainServiceImpl extends SaleOrderMergeControlServiceImpl {

  protected AppSaleService appSaleService;

  @Inject
  public SaleOrderMergeControlSupplychainServiceImpl(AppSaleService appSaleService) {
    this.appSaleService = appSaleService;
  }

  @Override
  protected void initCommonMap(
      Map<String, SaleOrderMergeObject> commonMap, SaleOrder firstSaleOrder) {
    super.initCommonMap(commonMap, firstSaleOrder);
    if (appSaleService.isApp("supplychain")) {
      commonMap.put(
          "stockLocation", new SaleOrderMergeObject(firstSaleOrder.getStockLocation(), false));
    }
  }
}
