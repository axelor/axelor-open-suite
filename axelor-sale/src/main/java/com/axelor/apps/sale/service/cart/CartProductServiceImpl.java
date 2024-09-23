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
package com.axelor.apps.sale.service.cart;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.Cart;
import com.axelor.apps.sale.db.CartLine;
import com.axelor.apps.sale.service.cartline.CartLineCreateService;
import com.axelor.apps.sale.service.cartline.CartLineRetrievalService;
import com.axelor.apps.sale.service.cartline.CartLineUpdateService;
import com.google.inject.Inject;

public class CartProductServiceImpl implements CartProductService {

  protected CartCreateService cartCreateService;
  protected CartRetrievalService cartRetrievalService;
  protected CartLineRetrievalService cartLineRetrievalService;
  protected CartLineCreateService cartLineCreateService;
  protected CartLineUpdateService cartLineUpdateService;

  @Inject
  public CartProductServiceImpl(
      CartCreateService cartCreateService,
      CartRetrievalService cartRetrievalService,
      CartLineRetrievalService cartLineRetrievalService,
      CartLineCreateService cartLineCreateService,
      CartLineUpdateService cartLineUpdateService) {
    this.cartCreateService = cartCreateService;
    this.cartRetrievalService = cartRetrievalService;
    this.cartLineRetrievalService = cartLineRetrievalService;
    this.cartLineCreateService = cartLineCreateService;
    this.cartLineUpdateService = cartLineUpdateService;
  }

  @Override
  public void addToCart(Product product) throws AxelorException {
    Cart cart = cartRetrievalService.getCurrentCart();
    if (cart == null) {
      cart = cartCreateService.createCart();
    }
    CartLine cartLine = cartLineRetrievalService.getCartLine(cart, product);
    if (cartLine == null) {
      cartLineCreateService.createCartLine(cart, product);
    } else {
      cartLineUpdateService.updateCartLine(cartLine);
    }
  }
}
