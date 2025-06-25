package com.axelor.apps.production.service;

import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.List;

public interface SolDetailsProdProcessLineUpdateService {
  boolean isSolDetailsUpdated(
      SaleOrderLine saleOrderLine, List<SaleOrderLineDetails> saleOrderLineDetailsList);
}
