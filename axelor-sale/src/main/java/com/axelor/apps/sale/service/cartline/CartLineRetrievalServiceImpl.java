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
package com.axelor.apps.sale.service.cartline;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.Cart;
import com.axelor.apps.sale.db.CartLine;
import com.axelor.apps.sale.db.repo.CartLineRepository;
import com.google.inject.Inject;

public class CartLineRetrievalServiceImpl implements CartLineRetrievalService {

  protected CartLineRepository cartLineRepository;

  @Inject
  public CartLineRetrievalServiceImpl(CartLineRepository cartLineRepository) {
    this.cartLineRepository = cartLineRepository;
  }

  @Override
  public CartLine getCartLine(Cart cart, Product product) {
    return cartLineRepository
        .all()
        .filter("self.product = :product AND self.cart = :cart")
        .bind("product", product)
        .bind("cart", cart)
        .fetchOne();
  }
}
