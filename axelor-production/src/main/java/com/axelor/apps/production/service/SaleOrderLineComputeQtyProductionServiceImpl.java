package com.axelor.apps.production.service;

import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeQtyServiceImpl;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineComputeQtyProductionServiceImpl
    extends SaleOrderLineComputeQtyServiceImpl {

  protected SaleOrderLineProductionService saleOrderLineProductionService;

  @Inject
  public SaleOrderLineComputeQtyProductionServiceImpl(
      SaleOrderLineProductionService saleOrderLineProductionService) {
    this.saleOrderLineProductionService = saleOrderLineProductionService;
  }

  @Override
  public Map<String, Object> initQty(SaleOrderLine saleOrderLine) {
    Map<String, Object> values = super.initQty(saleOrderLine);
    values.put(
        "qtyToProduce",
        saleOrderLineProductionService.computeQtyToProduce(
            saleOrderLine, saleOrderLine.getParentSaleOrderLine()));

    return values;
  }
}
