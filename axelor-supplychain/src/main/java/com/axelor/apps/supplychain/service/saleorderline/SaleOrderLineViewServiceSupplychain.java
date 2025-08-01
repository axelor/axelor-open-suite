package com.axelor.apps.supplychain.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineViewServiceSupplychain {

  Map<String, Map<String, Object>> hideDeliveryPanel(SaleOrderLine saleOrderLine);
}
