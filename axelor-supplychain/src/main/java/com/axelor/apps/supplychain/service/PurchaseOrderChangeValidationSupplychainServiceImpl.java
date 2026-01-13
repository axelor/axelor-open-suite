/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;

public class PurchaseOrderChangeValidationSupplychainServiceImpl
    implements PurchaseOrderChangeValidationSupplychainService {

  protected final PurchaseOrderReceiptStateService purchaseOrderReceiptStateService;
  protected final PurchaseOrderStockService purchaseOrderStockService;
  protected final AppSupplychainService appSupplychainService;

  @Inject
  public PurchaseOrderChangeValidationSupplychainServiceImpl(
      PurchaseOrderReceiptStateService purchaseOrderReceiptStateService,
      PurchaseOrderStockService purchaseOrderStockService,
      AppSupplychainService appSupplychainService) {
    this.purchaseOrderReceiptStateService = purchaseOrderReceiptStateService;
    this.purchaseOrderStockService = purchaseOrderStockService;
    this.appSupplychainService = appSupplychainService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validatePurchaseOrderChange(PurchaseOrder purchaseOrder) throws AxelorException {

    purchaseOrderReceiptStateService.updatePurchaseOrderLineReceiptState(purchaseOrder);
    purchaseOrderReceiptStateService.updateReceiptState(purchaseOrder);
    if (appSupplychainService.getAppSupplychain().getSupplierStockMoveGenerationAuto()) {
      purchaseOrderStockService.createStockMoveFromPurchaseOrder(purchaseOrder);
    }
  }
}
