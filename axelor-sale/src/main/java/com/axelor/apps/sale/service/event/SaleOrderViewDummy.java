package com.axelor.apps.sale.service.event;

import com.axelor.apps.sale.db.SaleOrder;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderViewDummy {
  private final SaleOrder saleOrder;
  private final Map<String, Object> saleOrderMap;

  public SaleOrderViewDummy(SaleOrder saleOrder) {
    this.saleOrder = saleOrder;
    this.saleOrderMap = new HashMap<>();
  }

  public SaleOrder getSaleOrder() {
    return saleOrder;
  }

  public Map<String, Object> getSaleOrderMap() {
    return saleOrderMap;
  }
}
