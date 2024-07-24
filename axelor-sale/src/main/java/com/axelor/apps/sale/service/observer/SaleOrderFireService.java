package com.axelor.apps.sale.service.observer;

import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderFireService {
  String confirmSaleOrder(SaleOrder saleOrder);
}
