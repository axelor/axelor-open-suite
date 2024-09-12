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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.Cart;
import com.axelor.apps.sale.db.CartLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.service.StockLocationLineFetchService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CartLineSupplychainServiceImpl implements CartLineSupplychainService {

  protected StockLocationLineFetchService stockLocationLineFetchService;
  protected AppBaseService appBaseService;

  @Inject
  public CartLineSupplychainServiceImpl(
      StockLocationLineFetchService stockLocationLineFetchService, AppBaseService appBaseService) {
    this.stockLocationLineFetchService = stockLocationLineFetchService;
    this.appBaseService = appBaseService;
  }

  @Override
  public CartLine setAvailableStatus(Cart cart, CartLine cartLine) {
    Product product = cartLine.getProduct();
    if (product != null && product.getIsModel() && cartLine.getVariantProduct() != null) {
      product = cartLine.getVariantProduct();
    }
    BigDecimal availableQty =
        stockLocationLineFetchService.getAvailableQty(cart.getStockLocation(), product);
    BigDecimal qty = cartLine.getQty();

    if (availableQty.compareTo(qty) >= 0) {
      cartLine.setAvailableStatus(I18n.get("Available"));
      cartLine.setAvailableStatusSelect(SaleOrderLineRepository.STATUS_AVAILABLE);
    } else {
      BigDecimal missingQty =
          availableQty
              .subtract(qty)
              .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP);
      cartLine.setAvailableStatus(I18n.get("Missing") + " (" + missingQty + ")");
      cartLine.setAvailableStatusSelect(SaleOrderLineRepository.STATUS_MISSING);
    }
    return cartLine;
  }
}
