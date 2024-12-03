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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.Cart;
import com.axelor.apps.sale.db.CartLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.supplychain.service.ProductStockLocationService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class CartLineAvailabilityServiceImpl implements CartLineAvailabilityService {

  protected ProductStockLocationService productStockLocationService;
  protected AppBaseService appBaseService;

  @Inject
  public CartLineAvailabilityServiceImpl(
      ProductStockLocationService productStockLocationService, AppBaseService appBaseService) {
    this.productStockLocationService = productStockLocationService;
    this.appBaseService = appBaseService;
  }

  @Override
  public void setAvailableStatus(Cart cart) throws AxelorException {
    List<CartLine> cartLineList = cart.getCartLineList();
    if (CollectionUtils.isEmpty(cartLineList)) {
      return;
    }
    for (CartLine cartLine : cartLineList) {
      setAvailableStatus(cart, cartLine);
    }
  }

  @Override
  public Map<String, Object> setAvailableStatus(Cart cart, CartLine cartLine)
      throws AxelorException {
    Product product =
        cartLine.getVariantProduct() != null ? cartLine.getVariantProduct() : cartLine.getProduct();
    String availableStatus = null;
    int availableStatusSelect = 0;

    if (product != null && product.getStockManaged()) {
      BigDecimal availableQty =
          productStockLocationService.getAvailableQty(
              product, cart.getCompany(), cart.getStockLocation());
      BigDecimal qty = cartLine.getQty();

      if (availableQty.compareTo(qty) >= 0) {
        availableStatus = I18n.get("Available");
        availableStatusSelect = SaleOrderLineRepository.STATUS_AVAILABLE;
      } else {
        BigDecimal missingQty =
            availableQty
                .subtract(qty)
                .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP);
        availableStatus = I18n.get("Missing") + " (" + missingQty + ")";
        availableStatusSelect = SaleOrderLineRepository.STATUS_MISSING;
      }
    }
    cartLine.setAvailableStatus(availableStatus);
    cartLine.setAvailableStatusSelect(availableStatusSelect);

    Map<String, Object> cartLineMap = new HashMap<>();
    cartLineMap.put("availableStatus", cartLine.getAvailableStatus());
    cartLineMap.put("availableStatusSelect", cartLine.getAvailableStatusSelect());
    return cartLineMap;
  }

  @Override
  public List<CartLine> getAvailableCartLineList(Cart cart, List<CartLine> cartLineList)
      throws AxelorException {
    List<CartLine> availableCartLineList = new ArrayList<>();
    for (CartLine cartLine : cartLineList) {
      Product product = cartLine.getProduct();
      if (product.getIsModel() && cartLine.getVariantProduct() != null) {
        product = cartLine.getVariantProduct();
      }
      BigDecimal availableQty =
          productStockLocationService.getAvailableQty(
              product, cart.getCompany(), cart.getStockLocation());
      if (availableQty.compareTo(cartLine.getQty()) >= 0) {
        availableCartLineList.add(cartLine);
      }
    }
    return availableCartLineList;
  }
}
