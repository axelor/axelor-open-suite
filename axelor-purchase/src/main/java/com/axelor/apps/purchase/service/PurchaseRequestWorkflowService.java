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
package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseRequest;

public interface PurchaseRequestWorkflowService {
  /**
   * Set the purchase request status to requested.
   *
   * @param purchaseRequest
   * @throws AxelorException
   */
  void requestPurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException;

  /**
   * Set the purchase request status to accepted.
   *
   * @param purchaseRequest
   * @throws AxelorException
   */
  void acceptPurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException;

  /**
   * Set the purchase request status to purchased.
   *
   * @param purchaseRequest
   * @throws AxelorException
   */
  void purchasePurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException;

  /**
   * Set the purchase request status to refused.
   *
   * @param purchaseRequest
   * @throws AxelorException
   */
  void refusePurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException;

  /**
   * Set the purchase request status to canceled.
   *
   * @param purchaseRequest
   * @throws AxelorException
   */
  void cancelPurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException;

  /**
   * Set the purchase request status to draft.
   *
   * @param purchaseRequest
   * @throws AxelorException
   */
  void draftPurchaseRequest(PurchaseRequest purchaseRequest) throws AxelorException;
}
