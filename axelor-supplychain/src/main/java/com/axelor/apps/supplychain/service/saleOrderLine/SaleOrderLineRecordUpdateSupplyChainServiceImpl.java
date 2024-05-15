package com.axelor.apps.supplychain.service.saleOrderLine;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.helper.SaleOrderLineHelper;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public class SaleOrderLineRecordUpdateSupplyChainServiceImpl
    implements SaleOrderLineRecordUpdateSupplyChainService {

  @Override
  public void updateRequestedReservedQty(
      SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    if (saleOrderLine.getRequestedReservedQty().compareTo(saleOrderLine.getQty()) > 0
        || saleOrderLine.getIsQtyRequested()) {
      SaleOrderLineHelper.addAttr(
          "requestedReservedQty", "value", BigDecimal.ZERO.max(saleOrderLine.getQty()), attrsMap);
    }
  }

  @Override
  public void hideUpdateAllocatedQtyBtn(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    SaleOrderLineHelper.addAttr(
        "updateAllocatedQtyBtn",
        "hidden",
        saleOrderLine.getId() == null
            || saleOrder.getStatusSelect() != 3
            || Objects.equals(saleOrderLine.getProduct().getProductTypeSelect(), "service"),
        attrsMap);
  }
}
