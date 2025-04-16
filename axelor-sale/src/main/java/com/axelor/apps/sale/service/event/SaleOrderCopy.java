package com.axelor.apps.sale.service.event;

import com.axelor.apps.sale.db.SaleOrder;

public class SaleOrderCopy {
  private final SaleOrder saleOrder;

  public SaleOrderCopy(SaleOrder saleOrder) {
    this.saleOrder = saleOrder;
  }

  public SaleOrder getSaleOrder() {
    return saleOrder;
  }
}
