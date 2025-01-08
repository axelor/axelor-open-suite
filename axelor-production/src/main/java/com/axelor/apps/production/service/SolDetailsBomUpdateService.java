package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrderLine;

public interface SolDetailsBomUpdateService {
  void updateSolDetailslWithBillOfMaterial(SaleOrderLine saleOrderLine) throws AxelorException;

  boolean isSolDetailsUpdated(SaleOrderLine saleOrderLine);
}
