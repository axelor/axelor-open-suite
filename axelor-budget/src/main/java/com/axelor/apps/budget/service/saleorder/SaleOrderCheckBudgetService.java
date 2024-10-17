package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderCheckBudgetService {
  String checkBudgetBeforeFinalize(SaleOrder saleOrder);

  void checkNoComputeBudgetError(SaleOrder saleOrder) throws AxelorException;
}
