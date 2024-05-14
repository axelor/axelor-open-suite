package com.axelor.apps.supplychain.service.saleOrderLine;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineGroupServiceImpl;
import com.axelor.apps.sale.service.saleorderline.XService;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineSupplyChainGroupServiceImpl extends SaleOrderLineGroupServiceImpl {

  @Inject
  public SaleOrderLineSupplyChainGroupServiceImpl(XService xService) {
    super(xService);
  }

  @Override
  public void getOnNewValuesMap(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    super.getOnNewValuesMap(saleOrder, saleOrderLine, attrsMap);
    xService.setIsReadOnlyValue(saleOrder, saleOrderLine, attrsMap);
    xService.hideUpdateAllocatedQtyBtn(saleOrder, saleOrderLine, attrsMap);
    xService.setRequestedReservedQtyTOReadOnly(saleOrder, attrsMap);
    xService.updateRequestedReservedQty(saleOrderLine, attrsMap);
  }
}
