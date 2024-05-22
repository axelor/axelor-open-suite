package com.axelor.apps.supplychain.service.saleOrderLine;

import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineRecordUpdateSupplyChainService {

  void updateRequestedReservedQty(
      SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap);

  void setAvailabilityRequestValue(
      SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap);
}
