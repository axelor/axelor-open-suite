/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.budget.service.purchaseorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;

public interface PurchaseOrderBudgetService {

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

  public void updateBudgetLinesFromPurchaseOrder(PurchaseOrder purchaseOrder);

  public void setPurchaseOrderLineBudget(PurchaseOrder purchaseOrder);

  public void applyToallBudgetDistribution(PurchaseOrder purchaseOrder);

  public void updateBudgetDistributionAmountAvailable(PurchaseOrder purchaseOrder);
}
