package com.axelor.apps.supplychain.service;

import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.TrackingNumber;

public interface TrackingNumberSupplychainService {

  void freeOriginSaleOrderLine(SaleOrderLine saleOrderLine);

  void freeOriginSaleOrderLine(TrackingNumber trackingNumber);
}
