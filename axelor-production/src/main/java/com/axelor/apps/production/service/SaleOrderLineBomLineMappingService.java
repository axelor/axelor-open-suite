package com.axelor.apps.production.service;

import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.sale.db.SaleOrderLine;

public interface SaleOrderLineBomLineMappingService {

  SaleOrderLine mapToSaleOrderLine(BillOfMaterialLine billOfMaterialLine);
}
