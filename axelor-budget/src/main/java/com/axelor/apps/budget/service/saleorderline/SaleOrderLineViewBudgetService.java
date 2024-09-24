package com.axelor.apps.budget.service.saleorderline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import java.util.Map;

public interface SaleOrderLineViewBudgetService {

  Map<String, Map<String, Object>> checkBudget(SaleOrder saleOrder) throws AxelorException;
}
