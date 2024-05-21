package com.axelor.apps.supplychain.service.saleOrderLine;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineAttrsSetSupplychainService {
  void setIsReadOnlyValue(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap);

  void setRequestedReservedQtyToReadOnly(
      SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap);

  void hideUpdateAllocatedQtyBtn(
          SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap);
}
