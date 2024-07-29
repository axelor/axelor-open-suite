package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;

public interface SaleOrderLineCheckSupplychainService {

  void saleSupplySelectOnChangeCheck(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;
}
