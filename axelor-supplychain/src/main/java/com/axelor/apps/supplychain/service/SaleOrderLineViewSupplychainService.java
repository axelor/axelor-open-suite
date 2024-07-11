package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineViewSupplychainService {
  Map<String, Map<String, Object>> getOnNewAttrs(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  Map<String, Map<String, Object>> getOnLoadAttrs(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;
}
