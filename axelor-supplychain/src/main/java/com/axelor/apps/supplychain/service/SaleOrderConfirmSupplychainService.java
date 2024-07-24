package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderConfirmSupplychainService {
  String confirmSaleOrder(SaleOrder saleOrder) throws AxelorException;
}
