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
package com.axelor.apps.supplychain.service.cart;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.service.cart.CartProductService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppSupplychain;
import com.google.inject.Inject;
import java.util.List;

public class CartStockLocationServiceImpl implements CartStockLocationService {

  protected CartProductService cartProductService;
  protected StockLocationLineRepository stockLocationLineRepository;
  protected AppSupplychainService appSupplychainService;

  @Inject
  public CartStockLocationServiceImpl(
      CartProductService cartProductService,
      StockLocationLineRepository stockLocationLineRepository,
      AppSupplychainService appSupplychainService) {
    this.cartProductService = cartProductService;
    this.stockLocationLineRepository = stockLocationLineRepository;
    this.appSupplychainService = appSupplychainService;
  }

  @Override
  public void addToCart(StockLocation stockLocation) throws AxelorException {
    AppSupplychain appSupplychain = appSupplychainService.getAppSupplychain();
    int cartLimit = appSupplychain.getStockLocationToCartLimit();

    List<StockLocationLine> stockLocationLineList =
        stockLocationLineRepository
            .all()
            .filter("self.stockLocation = :stockLocation")
            .bind("stockLocation", stockLocation)
            .fetch();
    if (stockLocationLineList.size() > cartLimit) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.STOCK_LOCATION_TO_CART_LIMIT_EXCEEDED),
          cartLimit);
    }

    for (StockLocationLine stockLocationLine : stockLocationLineList) {
      cartProductService.addToCart(stockLocationLine.getProduct());
    }
  }
}
