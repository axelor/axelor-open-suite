package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.List;

public interface SolDetailsBomUpdateService {
  void updateSolDetailslWithBillOfMaterial(
      SaleOrderLine saleOrderLine, List<SaleOrderLineDetails> saleOrderLineDetails)
      throws AxelorException;

  boolean isSolDetailsUpdated(
      SaleOrderLine saleOrderLine, List<SaleOrderLineDetails> saleOrderLineDetails);
}
