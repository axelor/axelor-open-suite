package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrderLine;

public interface SaleOrderLineCheckProductionService {
  void checkLinkedMo(SaleOrderLine saleOrderLine) throws AxelorException;
}
