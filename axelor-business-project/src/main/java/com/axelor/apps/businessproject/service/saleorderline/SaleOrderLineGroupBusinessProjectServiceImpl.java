package com.axelor.apps.businessproject.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorderline.XService;
import com.axelor.apps.supplychain.service.saleOrderLine.SaleOrderLineSupplyChainGroupServiceImpl;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineGroupBusinessProjectServiceImpl
    extends SaleOrderLineSupplyChainGroupServiceImpl {

  @Inject
  SaleOrderLineGroupBusinessProjectServiceImpl(XService xService) {
    super(xService);
  }

  @Override
  public void getOnNewValuesMap(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    super.getOnNewValuesMap(saleOrder, saleOrderLine, attrsMap);
    xService.setProjectTitle(attrsMap);
    xService.setProjectValue(saleOrder, attrsMap);
    xService.hideBillOfMaterialAndProdProcess(attrsMap);
    xService.setEstimatedDateValue(saleOrderLine, saleOrder, attrsMap);
  }
}
