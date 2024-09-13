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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.Cart;
import com.axelor.apps.sale.db.CartLine;
import com.axelor.apps.sale.service.CartLineProductServiceImpl;
import com.axelor.apps.sale.service.CartLineService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineProductService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.Map;

public class CartLineProductSupplychainServiceImpl extends CartLineProductServiceImpl {

  protected CartLineSupplychainService cartLineSupplychainService;

  @Inject
  public CartLineProductSupplychainServiceImpl(
      SaleOrderLineProductService saleOrderLineProductService,
      CartLineService cartLineService,
      CartLineSupplychainService cartLineSupplychainService) {
    super(saleOrderLineProductService, cartLineService);
    this.cartLineSupplychainService = cartLineSupplychainService;
  }

  @Override
  public Map<String, Object> getProductInformation(Cart cart, CartLine cartLine)
      throws AxelorException {
    Map<String, Object> cartLineMap = super.getProductInformation(cart, cartLine);
    Beans.get(CartLineSupplychainService.class).setAvailableStatus(cart, cartLine);
    cartLineMap.put("setAailableStatus", cartLine.getAvailableStatus());
    cartLineMap.put("setAailableStatusSelect", cartLine.getAvailableStatusSelect());
    return cartLineMap;
  }
}
