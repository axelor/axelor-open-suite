package com.axelor.apps.production.service;

import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderLineBomSyncService {
  void syncSaleOrderLineBom(SaleOrder saleOrder);
}
