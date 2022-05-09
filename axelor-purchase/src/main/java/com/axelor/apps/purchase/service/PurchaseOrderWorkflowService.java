/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.purchase.service;

import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.exception.AxelorException;

public interface PurchaseOrderWorkflowService {

  /**
   * Set the purchase order status to draft.
   *
   * @param purchaseOrder
   * @throws AxelorException
   */
  void draftPurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException;

  /**
   * Set the the validator and the validation date to the purchase and change the order status to
   * validated.
   *
   * @param purchaseOrder
   * @throws AxelorException
   */
  void validatePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException;

  /**
   * Set the purchase order status to finished.
   *
   * @param purchaseOrder
   * @throws AxelorException
   */
  void finishPurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException;

  /**
   * Set the purchase order status to canceled.
   *
   * @param purchaseOrder
   * @throws AxelorException
   */
  void cancelPurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException;
}
