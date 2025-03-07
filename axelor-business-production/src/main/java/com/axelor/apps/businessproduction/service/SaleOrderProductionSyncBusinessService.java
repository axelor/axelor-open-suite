package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrderLine;

public interface SaleOrderProductionSyncBusinessService {

  void syncSaleOrderLine(SaleOrderLine saleOrderLine) throws AxelorException;
}
