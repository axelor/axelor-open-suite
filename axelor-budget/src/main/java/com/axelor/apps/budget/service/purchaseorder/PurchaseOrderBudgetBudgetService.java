package com.axelor.apps.budget.service.purchaseorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;

public interface PurchaseOrderBudgetBudgetService {

  /**
   * For all budgets related to this purchase order, check budget exceed based on global budget
   * control on budget exceed then compute an error message if needed then return it.
   *
   * @param purchaseOrder
   * @return String
   */
  public String getBudgetExceedAlert(PurchaseOrder purchaseOrder);

  /**
   * For each purchase order line : Clear budget distribution, compute the budget key related to
   * this configuration of account and analytic, find the budget related to this key and the
   * purchase order order date. Then create an automatic budget distribution with the company ex tax
   * total and save the purchase order line. If a budget distribution is not generated, save the
   * purchase order line name in an alert message that will be return.
   *
   * @param purchaseOrder
   * @return String
   */
  public String computeBudgetDistribution(PurchaseOrder purchaseOrder);

  /**
   * Take all budget distribution on this purchase order and throw an error if the total amount of
   * budget distribution is superior to company ex tax total of the purchase order
   *
   * @param purchaseOrder
   * @throws AxelorException
   */
  public void validatePurchaseAmountWithBudgetDistribution(PurchaseOrder purchaseOrder)
      throws AxelorException;

  /**
   * Return if there is budget distribution on any purchase order line
   *
   * @param purchaseOrder
   * @return boolean
   */
  public boolean isBudgetInLines(PurchaseOrder purchaseOrder);

  // Duplicated from axelor-supplychain to prevent axelor-production dependency
  void generateBudgetDistribution(PurchaseOrder purchaseOrder);
}
