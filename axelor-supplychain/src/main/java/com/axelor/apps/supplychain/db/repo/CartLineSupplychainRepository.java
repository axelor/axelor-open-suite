/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.Cart;
import com.axelor.apps.sale.db.CartLine;
import com.axelor.apps.sale.db.repo.CartLineManagementRepository;
import com.axelor.apps.supplychain.service.cartline.CartLineAvailabilityService;
import com.google.inject.Inject;
import java.util.Map;

public class CartLineSupplychainRepository extends CartLineManagementRepository {

  protected CartLineAvailabilityService cartLineAvailabilityService;

  @Inject
  public CartLineSupplychainRepository(
      AppBaseService appBaseService, CartLineAvailabilityService cartLineAvailabilityService) {
    super(appBaseService);
    this.cartLineAvailabilityService = cartLineAvailabilityService;
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    try {
      if (json != null && json.get("id") != null) {
        CartLine cartLine = find((Long) json.get("id"));
        if (cartLine != null) {
          Cart cart = cartLine.getCart();
          json.putAll(cartLineAvailabilityService.setAvailableStatus(cart, cartLine));
        }
      }
      return super.populate(json, context);
    } catch (Exception e) {
      TraceBackService.trace(e);
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
