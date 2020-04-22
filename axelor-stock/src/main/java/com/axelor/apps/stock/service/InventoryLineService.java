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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockConfigRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.inject.Beans;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InventoryLineService {

  public InventoryLine createInventoryLine(
      Inventory inventory,
      Product product,
      BigDecimal currentQty,
      String rack,
      TrackingNumber trackingNumber) {

    InventoryLine inventoryLine = new InventoryLine();
    inventoryLine.setInventory(inventory);
    inventoryLine.setProduct(product);
    inventoryLine.setRack(rack);
    inventoryLine.setCurrentQty(currentQty);
    inventoryLine.setTrackingNumber(trackingNumber);
    this.compute(inventoryLine, inventory);

    return inventoryLine;
  }

  public InventoryLine updateInventoryLine(InventoryLine inventoryLine, Inventory inventory) {

    StockLocation stockLocation = inventory.getStockLocation();
    Product product = inventoryLine.getProduct();

    if (product != null) {
      StockLocationLine stockLocationLine =
          Beans.get(StockLocationLineService.class)
              .getOrCreateStockLocationLine(stockLocation, product);

      if (stockLocationLine != null) {
        inventoryLine.setCurrentQty(stockLocationLine.getCurrentQty());
        inventoryLine.setRack(stockLocationLine.getRack());
        if (inventoryLine.getTrackingNumber() != null) {
          inventoryLine.setCurrentQty(
              Beans.get(StockLocationLineRepository.class)
                  .all()
                  .filter(
                      "self.product = :product and self.detailsStockLocation = :stockLocation and self.trackingNumber = :trackingNumber")
                  .bind("product", inventoryLine.getProduct())
                  .bind("stockLocation", stockLocation)
                  .bind("trackingNumber", inventoryLine.getTrackingNumber())
                  .fetchStream()
                  .map(it -> it.getCurrentQty())
                  .reduce(BigDecimal.ZERO, (a, b) -> a.add(b)));
        }
      } else {
        inventoryLine.setCurrentQty(null);
        inventoryLine.setRack(null);
      }
    }

    return inventoryLine;
  }

  public InventoryLine compute(InventoryLine inventoryLine, Inventory inventory) {

    StockLocation stockLocation = inventory.getStockLocation();
    Product product = inventoryLine.getProduct();

    if (product != null) {
      inventoryLine.setUnit(product.getUnit());

      BigDecimal gap =
          inventoryLine.getRealQty() != null
              ? inventoryLine
                  .getCurrentQty()
                  .subtract(inventoryLine.getRealQty())
                  .setScale(2, RoundingMode.HALF_EVEN)
              : BigDecimal.ZERO;
      inventoryLine.setGap(gap);

      BigDecimal price;
      int value = stockLocation.getCompany().getStockConfig().getInventoryValuationTypeSelect();
      switch (value) {
        case StockConfigRepository.VALUATION_TYPE_ACCOUNTING_VALUE:
          price = product.getCostPrice();
          break;
        case StockConfigRepository.VALUATION_TYPE_SALE_VALUE:
          price = product.getSalePrice();
          break;
        default:
          price = product.getAvgPrice();
          break;
      }

      inventoryLine.setGapValue(gap.multiply(price).setScale(2, RoundingMode.HALF_EVEN));
      inventoryLine.setRealValue(
          inventoryLine.getRealQty() != null
              ? inventoryLine.getRealQty().multiply(price).setScale(2, RoundingMode.HALF_EVEN)
              : BigDecimal.ZERO
      );
    }

    return inventoryLine;
  }
}
