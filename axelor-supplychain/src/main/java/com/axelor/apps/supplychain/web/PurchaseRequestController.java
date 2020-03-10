/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.purchase.db.PurchaseRequest;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PurchaseRequestController {

  public void getStockLocation(ActionRequest request, ActionResponse response) {

    PurchaseRequest purchaseRequest = request.getContext().asType(PurchaseRequest.class);

    if (purchaseRequest.getCompany() != null) {

      response.setValue(
          "stockLocation",
          Beans.get(StockLocationService.class)
              .getDefaultReceiptStockLocation(purchaseRequest.getCompany()));
    }
  }
}
