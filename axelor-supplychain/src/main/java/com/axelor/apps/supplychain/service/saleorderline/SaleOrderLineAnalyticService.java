package com.axelor.apps.supplychain.service.saleorderline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineAnalyticService {
  Map<String, Object> printAnalyticAccounts(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException;
}
