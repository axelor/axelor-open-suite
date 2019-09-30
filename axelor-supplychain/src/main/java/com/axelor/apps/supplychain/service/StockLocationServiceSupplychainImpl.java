/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.stock.service.StockLocationServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

public class StockLocationServiceSupplychainImpl extends StockLocationServiceImpl
    implements StockLocationServiceSupplychain {

  @Inject
  public StockLocationServiceSupplychainImpl(
      StockLocationRepository stockLocationRepo,
      StockLocationLineService stockLocationLineService,
      ProductRepository productRepo) {
    super(stockLocationRepo, stockLocationLineService, productRepo);
  }

  @Override
  public BigDecimal getReservedQty(Long productId, Long locationId) throws AxelorException {
    if (productId != null) {
      Product product = productRepo.find(productId);
      Unit productUnit = product.getUnit();
      UnitConversionService unitConversionService = Beans.get(UnitConversionService.class);

      if (locationId == null) {
        List<StockLocation> stockLocations = getNonVirtualStockLocations();
        if (!stockLocations.isEmpty()) {
          BigDecimal reservedQty = BigDecimal.ZERO;
          for (StockLocation stockLocation : stockLocations) {
            StockLocationLine stockLocationLine =
                stockLocationLineService.getOrCreateStockLocationLine(
                    stockLocationRepo.find(stockLocation.getId()), productRepo.find(productId));

            if (stockLocationLine != null) {
              Unit stockLocationLineUnit = stockLocationLine.getUnit();
              reservedQty = reservedQty.add(stockLocationLine.getReservedQty());

              if (productUnit != null && !productUnit.equals(stockLocationLineUnit)) {
                reservedQty =
                    unitConversionService.convert(
                        stockLocationLineUnit,
                        productUnit,
                        reservedQty,
                        reservedQty.scale(),
                        product);
              }
            }
          }
          return reservedQty;
        }
      } else {
        StockLocationLine stockLocationLine =
            stockLocationLineService.getOrCreateStockLocationLine(
                stockLocationRepo.find(locationId), productRepo.find(productId));

        if (stockLocationLine != null) {
          Unit stockLocationLineUnit = stockLocationLine.getUnit();

          if (productUnit != null && !productUnit.equals(stockLocationLineUnit)) {
            return unitConversionService.convert(
                stockLocationLineUnit,
                productUnit,
                stockLocationLine.getReservedQty(),
                stockLocationLine.getReservedQty().scale(),
                product);
          }
          return stockLocationLine.getReservedQty();
        }
      }
    }
    return null;
  }
}
