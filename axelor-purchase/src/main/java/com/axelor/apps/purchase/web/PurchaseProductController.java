/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.ShippingCoefService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.math.BigDecimal;

@Singleton
public class PurchaseProductController {

  /**
   * Called from product form view, on {@link Product#defShipCoefByPartner} change. Call {@link
   * ShippingCoefService#getShippingCoef(Product, Partner, Company)}.
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
      BigDecimal productShippingCoef =
          Beans.get(ShippingCoefService.class)
              .getShippingCoef(
                  product,
                  product.getDefaultSupplierPartner(),
                  Beans.get(UserService.class).getUserActiveCompany());
      response.setValue("shippingCoef", productShippingCoef);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
