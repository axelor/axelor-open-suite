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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.cart.CartStockLocationService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import org.apache.commons.collections.CollectionUtils;

public class StockLocationController {

  public void addToCart(ActionRequest request, ActionResponse response) {
    try {
      StockLocation stockLocation = request.getContext().asType(StockLocation.class);
      if (CollectionUtils.isEmpty(stockLocation.getStockLocationLineList())) {
        return;
      }
      Beans.get(CartStockLocationService.class).addToCart(stockLocation);
      response.setNotify(
          String.format(
              I18n.get(SupplychainExceptionMessage.STOCK_LOCATION_PRODUCTS_ADDED_TO_CART),
              stockLocation.getName()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
