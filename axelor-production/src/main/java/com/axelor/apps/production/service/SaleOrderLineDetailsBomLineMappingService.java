package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderLineDetailsBomLineMappingService {

  SaleOrderLineDetails mapToSaleOrderLineDetails(
      BillOfMaterialLine billOfMaterialLine, SaleOrder saleOrder) throws AxelorException;
}
