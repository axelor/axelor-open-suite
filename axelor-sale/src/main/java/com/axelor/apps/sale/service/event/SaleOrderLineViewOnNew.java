package com.axelor.apps.sale.service.event;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineViewOnNew {
  private final SaleOrderLine saleOrderLine;
  private final SaleOrder saleOrder;
  private final Map<String, Map<String, Object>> saleOrderLineMap;

  public SaleOrderLineViewOnNew(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    this.saleOrderLine = saleOrderLine;
    this.saleOrder = saleOrder;
    this.saleOrderLineMap = new HashMap<>();
  }

  public SaleOrderLine getSaleOrderLine() {
    return saleOrderLine;
  }

  public SaleOrder getSaleOrder() {
    return saleOrder;
  }

  public Map<String, Map<String, Object>> getSaleOrderLineMap() {
    return saleOrderLineMap;
  }
}
