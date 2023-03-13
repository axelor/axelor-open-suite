package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;

public interface PurchaseOrderBudgetService {
  void generateBudgetDistribution(PurchaseOrder purchaseOrder);

  void applyToallBudgetDistribution(PurchaseOrder purchaseOrder);

  void setPurchaseOrderLineBudget(PurchaseOrder purchaseOrder);

  void updateBudgetDistributionAmountAvailable(PurchaseOrder purchaseOrder);

  /**
   * Check if budget distributions of the purchase order lines are correctly setted.
   *
   * @return true if it is good, else false
   * @param purchaseOrder
   */
  boolean isGoodAmountBudgetDistribution(PurchaseOrder purchaseOrder) throws AxelorException;
}
