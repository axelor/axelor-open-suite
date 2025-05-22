package com.axelor.apps.supplychain.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrderLine;

public interface SaleOrderLineBlockingSupplychainService {

  boolean isDeliveryBlocked(SaleOrderLine saleOrderLine);
}
