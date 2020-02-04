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
package com.axelor.apps.purchase.web;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.purchase.service.PurchaseProductService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class PurchaseProductController {

  /**
   * Called from product form view, on {@link Product#defShipCoefByPartner} change. Call {@link
   * PurchaseProductService#getLastShippingCoef(Product)}.
   *
   * @param request
   * @param response
   */
  public void fillShippingCoeff(ActionRequest request, ActionResponse response) {
    try {
      Product product = request.getContext().asType(Product.class);
      if (!product.getDefShipCoefByPartner()) {
        return;
      }
      response.setValue(
          "shippingCoef", Beans.get(PurchaseProductService.class).getLastShippingCoef(product));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
