package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderConfirmProductionService {
  void confirmProcess(SaleOrder saleOrder) throws AxelorException;
}
