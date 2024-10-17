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
package com.axelor.apps.supplychain.service.cartline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.Cart;
import com.axelor.apps.sale.db.CartLine;
import com.axelor.apps.sale.service.cartline.CartLinePriceService;
import com.axelor.apps.sale.service.cartline.CartLineProductServiceImpl;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineProductService;
import com.google.inject.Inject;
import java.util.Map;

public class CartLineProductSupplychainServiceImpl extends CartLineProductServiceImpl {

  protected CartLineAvailabilityService cartLineAvailabilityService;

  @Inject
  public CartLineProductSupplychainServiceImpl(
      SaleOrderLineProductService saleOrderLineProductService,
      CartLinePriceService cartLinePriceService,
      CartLineAvailabilityService cartLineAvailabilityService) {
    super(saleOrderLineProductService, cartLinePriceService);
    this.cartLineAvailabilityService = cartLineAvailabilityService;
  }

  @Override
  public Map<String, Object> getProductInformation(Cart cart, CartLine cartLine)
      throws AxelorException {
    Map<String, Object> cartLineMap = super.getProductInformation(cart, cartLine);
    cartLineMap.putAll(cartLineAvailabilityService.setAvailableStatus(cart, cartLine));
    return cartLineMap;
  }
}
