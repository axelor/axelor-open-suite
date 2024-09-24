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
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.CartRepository;
import com.axelor.apps.sale.service.cart.CartInitValueService;
import com.axelor.apps.sale.service.cart.CartResetService;
import com.axelor.apps.sale.service.cart.CartRetrievalService;
import com.axelor.apps.sale.service.cart.CartSaleOrderGeneratorService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Map;

public class CartController {

  public void setDefaultValues(ActionRequest request, ActionResponse response) {
    try {
      Cart cart = request.getContext().asType(Cart.class);
      Map<String, Object> values = Beans.get(CartInitValueService.class).getDefaultValues(cart);
      response.setValues(values);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void editCart(ActionRequest request, ActionResponse response) {
    try {
      Cart cart = Beans.get(CartRetrievalService.class).getCurrentCart();
      if (cart == null) {
        response.setView(
            ActionView.define(I18n.get("Cart"))
                .model(Cart.class.getName())
                .add("form", "cart-form")
                .map());
      } else {
        response.setView(
            ActionView.define(I18n.get("Cart"))
                .model(Cart.class.getName())
                .add("form", "cart-form")
                .param("forceEdit", "true")
                .context("_showRecord", String.valueOf(cart.getId()))
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void emptyCart(ActionRequest request, ActionResponse response) {
    try {
      Cart cart = request.getContext().asType(Cart.class);
      cart = Beans.get(CartRepository.class).find(cart.getId());
      Beans.get(CartResetService.class).emptyCart(cart);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void createSaleOrder(ActionRequest request, ActionResponse response) {
    try {
      Cart cart = request.getContext().asType(Cart.class);
      cart = Beans.get(CartRepository.class).find(cart.getId());
      SaleOrder saleOrder = Beans.get(CartSaleOrderGeneratorService.class).createSaleOrder(cart);
      response.setView(
          ActionView.define(I18n.get("Sale order"))
              .model(SaleOrder.class.getName())
              .add("form", "sale-order-form")
              .add("grid", "sale-order-grid")
              .context("_showRecord", String.valueOf(saleOrder.getId()))
              .map());
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
