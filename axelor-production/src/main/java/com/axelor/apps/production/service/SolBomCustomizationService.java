package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.sale.db.SaleOrderLine;

public interface SolBomCustomizationService {
  BillOfMaterial customizeBomOf(SaleOrderLine saleOrderLine) throws AxelorException;
}
