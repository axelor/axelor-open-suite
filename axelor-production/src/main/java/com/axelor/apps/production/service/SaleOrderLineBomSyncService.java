package com.axelor.apps.production.service;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;

public interface SaleOrderLineBomSyncService {
  void syncSaleOrderLineBom(SaleOrder saleOrder);

  void removeBomLines(SaleOrderLine saleOrderLine);
}
