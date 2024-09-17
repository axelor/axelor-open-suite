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
package com.axelor.apps.sale.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.Cart;
import com.axelor.apps.sale.db.CartLine;
import com.axelor.apps.sale.service.cartline.CartLineProductService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Map;

public class CartLineController {

  public void setProductInformation(ActionRequest request, ActionResponse response) {
    try {
      CartLine cartLine = request.getContext().asType(CartLine.class);
      Cart cart = request.getContext().getParent().asType(Cart.class);
      Map<String, Object> cartLineMap =
          Beans.get(CartLineProductService.class).getProductInformation(cart, cartLine);
      response.setValues(cartLineMap);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
