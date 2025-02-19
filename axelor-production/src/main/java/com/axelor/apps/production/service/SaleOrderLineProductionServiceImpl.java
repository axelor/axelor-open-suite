package com.axelor.apps.production.service;

import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;

public class SaleOrderLineProductionServiceImpl implements SaleOrderLineProductionService {
  @Override
  public BigDecimal computeQtyToProduce(SaleOrderLine saleOrderLine, SaleOrderLine parentSol) {
    BigDecimal produceQty = saleOrderLine.getQty();
    if (parentSol != null) {
      produceQty = produceQty.multiply(parentSol.getQtyToProduce());
    }
    return produceQty;
  }
}
