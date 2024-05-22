package com.axelor.apps.supplychain.service.saleOrderLine;

import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;

public class SaleOrderLineInitialValuesSupplyChainServiceImpl
    implements SaleOrderLineInitialValuesSupplyChainService {

  @Override
  public BigDecimal updateRequestedReservedQty(SaleOrderLine saleOrderLine) {
    BigDecimal requestedReservedQty = saleOrderLine.getRequestedReservedQty();
    if (saleOrderLine.getRequestedReservedQty().compareTo(saleOrderLine.getQty()) > 0
        || saleOrderLine.getIsQtyRequested()) {
      requestedReservedQty = BigDecimal.ZERO.max(saleOrderLine.getQty());
    }
    return requestedReservedQty;
  }
}
