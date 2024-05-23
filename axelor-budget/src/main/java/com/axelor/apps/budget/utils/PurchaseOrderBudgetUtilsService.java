/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.utils;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;

public interface PurchaseOrderBudgetUtilsService {

  // Duplicated from axelor-supplychain to prevent axelor-production dependency
  void generateBudgetDistribution(PurchaseOrder purchaseOrder);

  /**
   * Take all budget distribution on this purchase order and throw an error if the total amount of
   * budget distribution is superior to company ex tax total of the purchase order
   *
   * @param purchaseOrder
   * @throws AxelorException
   */
  void validatePurchaseAmountWithBudgetDistribution(PurchaseOrder purchaseOrder)
      throws AxelorException;

  void updateBudgetLinesFromPurchaseOrder(PurchaseOrder purchaseOrder);
}
