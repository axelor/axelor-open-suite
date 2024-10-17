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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.cart.CartProductService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProductController {

  public void addToCart(ActionRequest request, ActionResponse response) {
    try {
      Product product = request.getContext().asType(Product.class);
      Beans.get(CartProductService.class).addToCart(product);
      response.setNotify(
          String.format(I18n.get(SaleExceptionMessage.PRODUCT_ADDED_TO_CART), product.getName()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
