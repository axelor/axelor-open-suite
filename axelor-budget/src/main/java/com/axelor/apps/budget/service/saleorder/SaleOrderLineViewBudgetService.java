package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineViewBudgetService {
  Map<String, Map<String, Object>> getOnNewAttrs(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  Map<String, Map<String, Object>> getOnLoadAttrs(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;
}
