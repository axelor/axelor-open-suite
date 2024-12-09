package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrderLine;

public interface SolBomUpdateService {
  void updateSolWithBillOfMaterial(SaleOrderLine saleOrderLine) throws AxelorException;

  boolean isUpdated(SaleOrderLine saleOrderLine);
}
