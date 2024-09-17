package com.axelor.apps.supplychain.service.saleorder.status;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderConfirmSupplychainService {
  String confirmProcess(SaleOrder saleOrder) throws AxelorException;
}
