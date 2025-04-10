package com.axelor.apps.production.service;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineProductSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.view.SaleOrderLineOnSaleSupplyChangeServiceImpl;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineOnSaleSupplyChangeServiceProductionImpl
    extends SaleOrderLineOnSaleSupplyChangeServiceImpl {

  protected SaleOrderLineProductionViewService saleOrderLineProductionViewService;

  @Inject
  public SaleOrderLineOnSaleSupplyChangeServiceProductionImpl(
      SaleOrderLineProductSupplychainService saleOrderLineProductSupplychainService,
      SaleOrderLineProductionViewService saleOrderLineProductionViewService) {
    super(saleOrderLineProductSupplychainService);
    this.saleOrderLineProductionViewService = saleOrderLineProductionViewService;
  }

  @Override
  public Map<String, Map<String, Object>> onSaleSupplyChangeAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs =
        super.onSaleSupplyChangeAttrs(saleOrderLine, saleOrder);
    attrs.putAll(saleOrderLineProductionViewService.hideBomAndProdProcess(saleOrderLine));
    return attrs;
  }
}
