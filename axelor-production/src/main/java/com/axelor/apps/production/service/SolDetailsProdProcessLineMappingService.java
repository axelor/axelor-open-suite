package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;

public interface SolDetailsProdProcessLineMappingService {
  SaleOrderLineDetails mapToSaleOrderLineDetails(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, ProdProcessLine prodProcessLine)
      throws AxelorException;
}
