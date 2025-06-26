package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrderLine;

public interface SolDetailsProdProcessComputeQtyService {
  void setQty(
      SaleOrderLine saleOrderLine,
      ProdProcessLine prodProcessLine,
      SaleOrderLineDetails saleOrderLineDetails)
      throws AxelorException;
}
