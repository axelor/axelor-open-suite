package com.axelor.apps.production.service;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineAnalyticService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineInitValueSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineServiceSupplyChain;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineInitValueProductionServiceImpl
    extends SaleOrderLineInitValueSupplychainServiceImpl {

  protected SaleOrderLineProductionService saleOrderLineProductionService;

  @Inject
  public SaleOrderLineInitValueProductionServiceImpl(
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain,
      AppSupplychainService appSupplychainService,
      SaleOrderLineAnalyticService saleOrderLineAnalyticService,
      SaleOrderLineProductionService saleOrderLineProductionService) {
    super(saleOrderLineServiceSupplyChain, appSupplychainService, saleOrderLineAnalyticService);
    this.saleOrderLineProductionService = saleOrderLineProductionService;
  }

  @Override
  public Map<String, Object> onNewEditableInitValues(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, SaleOrderLine parentSol) {
    Map<String, Object> values = super.onNewEditableInitValues(saleOrder, saleOrderLine, parentSol);
    values.put(
        "qtyToProduce",
        saleOrderLineProductionService.computeQtyToProduce(
            saleOrderLine, saleOrderLine.getParentSaleOrderLine()));
    return values;
  }
}
