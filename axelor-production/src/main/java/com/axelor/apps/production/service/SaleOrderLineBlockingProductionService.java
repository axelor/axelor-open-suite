package com.axelor.apps.production.service;

import com.axelor.apps.sale.db.SaleOrderLine;

public interface SaleOrderLineBlockingProductionService {

  boolean isProductionBlocked(SaleOrderLine saleOrderLine);
}
