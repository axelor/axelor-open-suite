package com.axelor.apps.sale.service.observer;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineFireService {
  Map<String, Map<String, Object>> getOnNewAttrs(SaleOrderLine saleOrderLine, SaleOrder saleOrder);

  Map<String, Map<String, Object>> getOnLoadAttrs(SaleOrderLine saleOrderLine, SaleOrder saleOrder);
}
