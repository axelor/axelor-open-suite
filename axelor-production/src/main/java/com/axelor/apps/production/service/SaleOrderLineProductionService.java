package com.axelor.apps.production.service;

import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;

public interface SaleOrderLineProductionService {
  BigDecimal computeQtyToProduce(SaleOrderLine saleOrderLine, SaleOrderLine parentSol);
}
