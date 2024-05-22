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
package com.axelor.apps.stock.utils;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;

public class StockMoveLineUtilsServiceImpl implements StockMoveLineUtilsService {

  protected ProductCompanyService productCompanyService;

  @Inject
  public StockMoveLineUtilsServiceImpl(ProductCompanyService productCompanyService) {
    this.productCompanyService = productCompanyService;
  }

  @Override
  public void setAvailableStatus(StockMoveLine stockMoveLine) throws AxelorException {
    if (stockMoveLine.getStockMove() != null) {
      updateAvailableQty(stockMoveLine, stockMoveLine.getFromStockLocation());
    }
    if (stockMoveLine.getProduct() != null
        && !stockMoveLine
            .getProduct()
            .getProductTypeSelect()
            .equals(ProductRepository.PRODUCT_TYPE_SERVICE)) {
      BigDecimal availableQty = stockMoveLine.getAvailableQty();
      BigDecimal availableQtyForProduct = stockMoveLine.getAvailableQtyForProduct();
      BigDecimal realQty = stockMoveLine.getRealQty();
      if (availableQty.compareTo(realQty) >= 0 || !stockMoveLine.getProduct().getStockManaged()) {
        stockMoveLine.setAvailableStatus(I18n.get("Available"));
        stockMoveLine.setAvailableStatusSelect(StockMoveLineRepository.STATUS_AVAILABLE);
      } else if (availableQtyForProduct.compareTo(realQty) >= 0) {
        stockMoveLine.setAvailableStatus(I18n.get("Av. for product"));
        stockMoveLine.setAvailableStatusSelect(
            StockMoveLineRepository.STATUS_AVAILABLE_FOR_PRODUCT);
      } else if (availableQty.compareTo(realQty) < 0
          && availableQtyForProduct.compareTo(realQty) < 0) {
        BigDecimal missingQty = BigDecimal.ZERO;
        TrackingNumberConfiguration trackingNumberConfiguration =
            (TrackingNumberConfiguration)
                productCompanyService.get(
                    stockMoveLine.getProduct(),
                    "trackingNumberConfiguration",
                    Optional.ofNullable(stockMoveLine.getStockMove())
                        .map(StockMove::getCompany)
                        .orElse(null));

        if (trackingNumberConfiguration != null) {
          missingQty = availableQtyForProduct.subtract(realQty);
        } else {
          missingQty = availableQty.subtract(realQty);
        }
        stockMoveLine.setAvailableStatus(I18n.get("Missing") + " (" + missingQty + ")");
        stockMoveLine.setAvailableStatusSelect(StockMoveLineRepository.STATUS_MISSING);
      }
    }
  }

  @Override
  public void updateAvailableQty(StockMoveLine stockMoveLine, StockLocation stockLocation)
      throws AxelorException {
    BigDecimal availableQty = BigDecimal.ZERO;
    BigDecimal availableQtyForProduct = BigDecimal.ZERO;

    TrackingNumberConfiguration trackingNumberConfiguration =
        (TrackingNumberConfiguration)
            productCompanyService.get(
                stockMoveLine.getProduct(),
                "trackingNumberConfiguration",
                Optional.ofNullable(stockMoveLine.getStockMove())
                    .map(StockMove::getCompany)
                    .orElse(null));

    StockLocationLineService stockLocationLineService = Beans.get(StockLocationLineService.class);

    if (stockMoveLine.getProduct() != null) {
      if (trackingNumberConfiguration != null) {

        if (stockMoveLine.getTrackingNumber() != null) {
          StockLocationLine stockLocationLine =
              stockLocationLineService.getDetailLocationLine(
                  stockLocation, stockMoveLine.getProduct(), stockMoveLine.getTrackingNumber());

          if (stockLocationLine != null) {
            availableQty = stockLocationLine.getCurrentQty();
          }
        }

        if (availableQty.compareTo(stockMoveLine.getRealQty()) < 0) {
          StockLocationLine stockLocationLineForProduct =
              stockLocationLineService.getStockLocationLine(
                  stockLocation, stockMoveLine.getProduct());

          if (stockLocationLineForProduct != null) {
            availableQtyForProduct = stockLocationLineForProduct.getCurrentQty();
          }
        }
      } else {
        StockLocationLine stockLocationLine =
            stockLocationLineService.getStockLocationLine(
                stockLocation, stockMoveLine.getProduct());

        if (stockLocationLine != null) {
          availableQty = stockLocationLine.getCurrentQty();
        }
      }
    }
    stockMoveLine.setAvailableQty(availableQty);
    stockMoveLine.setAvailableQtyForProduct(availableQtyForProduct);
  }
}
