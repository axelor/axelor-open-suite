package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderConfirmProductionService {
  void confirmSaleOrder(SaleOrder saleOrder) throws AxelorException;
}
