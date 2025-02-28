package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrderLine;

public class SaleOrderLineUtils {

  private SaleOrderLineUtils() {}

  public static SaleOrderLine getParentSol(SaleOrderLine saleOrderLine) {
    SaleOrderLine parentSaleOrderLine = saleOrderLine.getParentSaleOrderLine();
    if (parentSaleOrderLine != null) {
      return getParentSol(parentSaleOrderLine);
    } else {
      if (saleOrderLine.getSaleOrder() != null) {
        return saleOrderLine;
      }
    }
    return saleOrderLine;
  }
}
