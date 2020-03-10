/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.WapHistoryRepository;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.stock.service.StockLocationLineServiceImpl;
import com.axelor.apps.stock.service.StockRulesService;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;

@RequestScoped
public class StockLocationLineServiceSupplychainImpl extends StockLocationLineServiceImpl {

  protected AppSupplychainService appSupplychainService;

  @Inject
  public StockLocationLineServiceSupplychainImpl(
      StockLocationLineRepository stockLocationLineRepo,
      StockRulesService stockRulesService,
      StockMoveLineRepository stockMoveLineRepository,
      AppBaseService appBaseService,
      WapHistoryRepository wapHistoryRepo,
      AppSupplychainService appSupplychainService) {
    super(
        stockLocationLineRepo,
        stockRulesService,
        stockMoveLineRepository,
        appBaseService,
        wapHistoryRepo);
    this.appSupplychainService = appSupplychainService;
  }

  @Override
  public void checkIfEnoughStock(StockLocation stockLocation, Product product, BigDecimal qty)
      throws AxelorException {
    super.checkIfEnoughStock(stockLocation, product, qty);

    AppSupplychainService appSupplychainService = Beans.get(AppSupplychainService.class);

    if (!appSupplychainService.isApp("supplychain")) {
      return;
    }
    if (appSupplychainService.getAppSupplychain().getManageStockReservation()
        && product.getStockManaged()) {
      StockLocationLine stockLocationLine = this.getStockLocationLine(stockLocation, product);
      if (stockLocationLine != null
          && stockLocationLine
                  .getCurrentQty()
                  .subtract(stockLocationLine.getReservedQty())
                  .compareTo(qty)
              < 0) {
        throw new AxelorException(
            stockLocationLine,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.LOCATION_LINE_RESERVED_QTY),
            stockLocationLine.getProduct().getName(),
            stockLocationLine.getProduct().getCode());
      }
    }
  }

  @Override
  public BigDecimal getAvailableQty(StockLocation stockLocation, Product product) {

    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return super.getAvailableQty(stockLocation, product);
    }

    StockLocationLine stockLocationLine = getStockLocationLine(stockLocation, product);
    BigDecimal availableQty = BigDecimal.ZERO;
    if (stockLocationLine != null) {
      availableQty = stockLocationLine.getCurrentQty().subtract(stockLocationLine.getReservedQty());
    }
    return availableQty;
  }

  @Override
  public BigDecimal getTrackingNumberAvailableQty(
      StockLocation stockLocation, TrackingNumber trackingNumber) {

    if (!appSupplychainService.isApp("supplychain")
        || !appSupplychainService.getAppSupplychain().getManageStockReservation()) {
      return super.getTrackingNumberAvailableQty(stockLocation, trackingNumber);
    }

    StockLocationLine detailStockLocationLine =
        Beans.get(StockLocationLineService.class)
            .getDetailLocationLine(stockLocation, trackingNumber.getProduct(), trackingNumber);

    BigDecimal availableQty = BigDecimal.ZERO;

    if (detailStockLocationLine != null) {
      availableQty =
          detailStockLocationLine
              .getCurrentQty()
              .subtract(detailStockLocationLine.getReservedQty());
    }
    return availableQty;
  }

  @Override
  public StockLocationLine updateLocationFromProduct(
      StockLocationLine stockLocationLine, Product product) throws AxelorException {
    stockLocationLine = super.updateLocationFromProduct(stockLocationLine, product);

    if (Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      Beans.get(ReservedQtyService.class).updateRequestedReservedQty(stockLocationLine);
    }

    return stockLocationLine;
  }
}
