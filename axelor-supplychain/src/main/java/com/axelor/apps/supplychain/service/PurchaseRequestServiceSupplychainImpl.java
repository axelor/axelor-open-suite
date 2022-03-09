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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseRequest;
import com.axelor.apps.purchase.service.PurchaseRequestServiceImpl;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class PurchaseRequestServiceSupplychainImpl extends PurchaseRequestServiceImpl {

  @Inject StockLocationRepository stockLocationRepo;

  @Override
  protected PurchaseOrder createPurchaseOrder(PurchaseRequest purchaseRequest)
      throws AxelorException {

    PurchaseOrder purchaseOrder = super.createPurchaseOrder(purchaseRequest);

    if (Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      purchaseOrder.setStockLocation(purchaseRequest.getStockLocation());
    }
    return purchaseOrder;
  }

  @Override
  protected String getPurchaseOrderGroupBySupplierKey(PurchaseRequest purchaseRequest) {
    String key = super.getPurchaseOrderGroupBySupplierKey(purchaseRequest);

    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return key;
    }

    StockLocation stockLocation = purchaseRequest.getStockLocation();
    if (stockLocation != null) {
      key = key + "_" + stockLocation.getId().toString();
    }
    return key;
  }
}
