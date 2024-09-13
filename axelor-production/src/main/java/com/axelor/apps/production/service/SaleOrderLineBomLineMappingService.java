package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;

public interface SaleOrderLineBomLineMappingService {

  SaleOrderLine mapToSaleOrderLine(BillOfMaterialLine billOfMaterialLine, SaleOrder saleOrder)
      throws AxelorException;
}
