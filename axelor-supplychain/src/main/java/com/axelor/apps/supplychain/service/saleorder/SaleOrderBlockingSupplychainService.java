package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderBlockingSupplychainService {
  boolean hasOnGoingBlocking(SaleOrder saleOrder);
}
