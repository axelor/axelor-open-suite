package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderBudgetService {

  void generateBudgetDistribution(SaleOrder saleOrder);

  String computeBudgetDistribution(SaleOrder saleOrder);

  void validateSaleAmountWithBudgetDistribution(SaleOrder saleOrder) throws AxelorException;

  boolean isBudgetInLines(SaleOrder saleOrder);

  public void updateBudgetLinesFromSaleOrder(SaleOrder saleOrder);
}
