package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineProductBudgetService {
  Map<String, Object> computeProductInformationBudget(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException;
}
