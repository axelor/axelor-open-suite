package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.List;

public interface SaleOrderLineBudgetService {

  List<BudgetDistribution> addBudgetDistribution(SaleOrderLine saleOrderLine);

  void fillBudgetStrOnLine(SaleOrderLine saleOrderLine, boolean multiBudget);

  String searchAndFillBudgetStr(SaleOrderLine saleOrderLine, boolean multiBudget);

  String computeBudgetDistribution(SaleOrderLine saleOrderLine);

  String getBudgetDomain(SaleOrderLine saleOrderLine);

  void checkAmountForSaleOrderLine(SaleOrderLine saleOrderLine) throws AxelorException;

  void computeBudgetDistributionSumAmount(SaleOrderLine saleOrderLine, SaleOrder saleOrder);

  String getGroupBudgetDomain(SaleOrderLine saleOrderLine, SaleOrder saleOrder);

  String getSectionBudgetDomain(SaleOrderLine saleOrderLine, SaleOrder saleOrder);

  String getLineBudgetDomain(SaleOrderLine saleOrderLine, SaleOrder saleOrder, boolean isBudget);
}
