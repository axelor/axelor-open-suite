package com.axelor.apps.supplychain.service.saleOrderLine;

import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;

public interface SaleOrderLineInitialValuesSupplyChainService {
  BigDecimal updateRequestedReservedQty(SaleOrderLine saleOrderLine);
}
