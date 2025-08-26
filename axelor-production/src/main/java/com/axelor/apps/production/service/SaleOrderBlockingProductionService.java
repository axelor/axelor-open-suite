package com.axelor.apps.production.service;

import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderBlockingProductionService {

  boolean hasOnGoingBlocking(SaleOrder saleOrder);
}
