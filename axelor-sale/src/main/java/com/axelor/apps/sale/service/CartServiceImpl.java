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
package com.axelor.apps.sale.service;

import com.axelor.apps.sale.db.Cart;
import com.axelor.apps.sale.db.repo.CartRepository;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.apache.commons.collections.CollectionUtils;

public class CartServiceImpl implements CartService {

  protected CartRepository cartRepository;

  @Inject
  public CartServiceImpl(CartRepository cartRepository) {
    this.cartRepository = cartRepository;
  }

  @Override
  public Cart getCurrentCart() {
    return cartRepository
        .all()
        .filter("self.user = :user")
        .bind("user", AuthUtils.getUser())
        .order("-createdOn")
        .fetchOne();
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void emptyCart(Cart cart) {
    if (!CollectionUtils.isEmpty(cart.getCartLineList())) {
      cart.getCartLineList().clear();
    }
    cart.setPartner(null);
    cartRepository.save(cart);
  }
}
