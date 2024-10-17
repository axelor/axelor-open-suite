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
package com.axelor.apps.production.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.ProdProductAttrsService;
import com.axelor.apps.production.service.ProdProductService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProdProductController {

  @ErrorException
  public void setTrackingNumberDomain(ActionRequest request, ActionResponse response)
      throws AxelorException {
    var prodProduct = request.getContext().asType(ProdProduct.class);
    var parent = request.getContext().getParent();

    if (parent != null && parent.getContextClass().equals(ManufOrder.class)) {
      var manufOrder = parent.asType(ManufOrder.class);
      response.setAttr(
          "wasteProductTrackingNumber",
          "domain",
          Beans.get(ProdProductAttrsService.class)
              .getTrackingNumberDomain(manufOrder, prodProduct));
    }
  }

  public void checkFinishedProduct(ActionRequest request, ActionResponse response)
      throws AxelorException {

    var prodProduct = request.getContext().asType(ProdProduct.class);
    var parent = request.getContext().getParent();

    if (parent != null
        && parent.getContextClass().equals(ManufOrder.class)
        && Beans.get(ProdProductService.class)
            .existInFinishedProduct(parent.asType(ManufOrder.class), prodProduct)) {
      response.setInfo(ProductionExceptionMessage.MANUF_ORDER_WASTE_DECLARATION_IN_PRODUCED_LIST);
    }
  }
}
