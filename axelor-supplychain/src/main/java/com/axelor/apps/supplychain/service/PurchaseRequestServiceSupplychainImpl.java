/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.util.List;

public class PurchaseRequestServiceSupplychainImpl extends PurchaseRequestServiceImpl {

  @Inject StockLocationRepository stockLocationRepo;

  @Override
  protected PurchaseOrder getPoBySupplierAndDeliveryAddress(
      PurchaseRequest purchaseRequest, List<PurchaseOrder> purchaseOrderList) {

    PurchaseOrder purchaseOrder =
        super.getPoBySupplierAndDeliveryAddress(purchaseRequest, purchaseOrderList);
    List<StockLocation> stockLocations =
        stockLocationRepo
            .all()
            .filter("self.address = ?1", purchaseRequest.getDeliveryAddress())
            .fetch();
    StockLocation stockLocation = stockLocations.size() == 1 ? stockLocations.get(0) : null;
    purchaseOrder =
        stockLocation != null
                && purchaseOrder != null
                && purchaseOrder.getStockLocation().equals(stockLocation)
            ? purchaseOrder
            : null;
    return purchaseOrder;
  }

  @Override
  protected PurchaseOrder createPurchaseOrder(PurchaseRequest purchaseRequest)
      throws AxelorException {

    PurchaseOrder purchaseOrder = super.createPurchaseOrder(purchaseRequest);
    StockLocation stockLocation =
        stockLocationRepo
            .all()
            .filter("self.address = ?1", purchaseRequest.getDeliveryAddress())
            .fetchOne();
    purchaseOrder.setStockLocation(stockLocation);
    return purchaseOrder;
  }
}
