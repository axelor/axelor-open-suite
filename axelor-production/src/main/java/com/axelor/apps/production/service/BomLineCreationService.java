package com.axelor.apps.production.service;

import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrderLine;

public interface BomLineCreationService {
  BillOfMaterialLine createBomLineFromSol(SaleOrderLine saleOrderLine);

  BillOfMaterialLine createBomLineFromSolDetails(SaleOrderLineDetails saleOrderLineDetails);
}
