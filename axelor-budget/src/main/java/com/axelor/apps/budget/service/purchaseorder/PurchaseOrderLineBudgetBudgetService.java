package com.axelor.apps.budget.service.purchaseorder;

import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import java.util.List;

public interface PurchaseOrderLineBudgetBudgetService {

  /**
   * Clear budget distribution, compute the budget key related to this configuration of account and
   * analytic, find the budget related to this key and the purchase order date. Then create an
   * automatic budget distribution with the company ex tax total and save the purchase order line.
   * Return an error message if a budget distribution is not generated
   *
   * @param purchaseOrderLine
   * @return String
   */
  public String computeBudgetDistribution(PurchaseOrderLine purchaseOrderLine);

  /**
   * If multi budget, compute budget distribution line's budget name to fill budget name string
   * field Else, fill budget name string with the budget's name
   *
   * @param purchaseOrderLine, multiBudget
   */
  public void fillBudgetStrOnLine(PurchaseOrderLine purchaseOrderLine, boolean multiBudget);

  /**
   * If mono budget and budget not null, create a budget distribution line with budget link and ex
   * tax total as amount
   *
   * @param purchaseOrderLine
   * @return List BudgetDistribution
   */
  public List<BudgetDistribution> addBudgetDistribution(PurchaseOrderLine purchaseOrderLine);

  /**
   * If multi budget, compute budget distribution line's budget name to fill budget name string
   * field Else, fill budget name string with the budget's name
   *
   * @param purchaseOrderLine, multiBudget
   * @return String
   */
  public String searchAndFillBudgetStr(PurchaseOrderLine purchaseOrderLine, boolean multiBudget);

  /**
   * Get domain for budget field via purchase order line using group, section, line from it and
   * order date from purchase order
   *
   * @param purchaseOrderLine, purchaseOrder
   * @return String
   */
  public String getBudgetDomain(PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder);

  /**
   * Take all budget distribution on this purchase order line and throw an error if the total amount
   * of budget distribution is superior to company ex tax total of the purchase order line
   *
   * @param purchaseOrderLine
   * @throws AxelorException
   */
  public void checkAmountForPurchaseOrderLine(PurchaseOrderLine purchaseOrderLine)
      throws AxelorException;
}
