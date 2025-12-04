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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

public class InventoryStockLocationUpdateServiceImpl
    implements InventoryStockLocationUpdateService {

  protected final StockLocationLineRepository stockLocationLineRepository;

  @Inject
  public InventoryStockLocationUpdateServiceImpl(
      StockLocationLineRepository stockLocationLineRepository) {
    this.stockLocationLineRepository = stockLocationLineRepository;
  }

  @Override
  public void storeLastInventoryData(Inventory inventory) {
    List<InventoryLine> inventoryLineList = inventory.getInventoryLineList();
    if (CollectionUtils.isEmpty(inventoryLineList)) {
      return;
    }

    List<Product> productList =
        inventoryLineList.stream()
            .map(InventoryLine::getProduct)
            .distinct()
            .collect(Collectors.toList());
    StockLocation stockLocation = inventory.getStockLocation();

    updateStockLocationLines(inventory, stockLocation, productList, inventoryLineList);
    updateDetailsStockLocationLines(inventory, stockLocation, productList, inventoryLineList);
  }

  protected void updateStockLocationLines(
      Inventory inventory,
      StockLocation stockLocation,
      List<Product> productList,
      List<InventoryLine> inventoryLineList) {
    List<StockLocationLine> stockLocationLineList =
        stockLocationLineRepository
            .all()
            .filter(
                "self.stockLocation = :stockLocation"
                    + " AND self.product IN :productList"
                    + " AND self.trackingNumber IS NULL")
            .bind("stockLocation", stockLocation)
            .bind("productList", productList)
            .fetch();

    Map<Product, BigDecimal> productToQtyMap = getProductQtyMap(inventoryLineList);

    for (StockLocationLine stockLocationLine : stockLocationLineList) {
      Product product = stockLocationLine.getProduct();
      BigDecimal realQty = productToQtyMap.getOrDefault(product, BigDecimal.ZERO);
      updateLine(inventory, inventoryLineList, stockLocationLine, realQty, product);
    }
  }

  protected Map<Product, BigDecimal> getProductQtyMap(List<InventoryLine> inventoryLineList) {
    return inventoryLineList.stream()
        .filter(line -> line.getTrackingNumber() == null)
        .collect(
            Collectors.groupingBy(
                InventoryLine::getProduct,
                Collectors.mapping(
                    InventoryLine::getRealQty,
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
  }

  protected void updateDetailsStockLocationLines(
      Inventory inventory,
      StockLocation stockLocation,
      List<Product> productList,
      List<InventoryLine> inventoryLineList) {
    List<StockLocationLine> detailsStockLocationLineList =
        stockLocationLineRepository
            .all()
            .filter(
                "self.detailsStockLocation = :stockLocation"
                    + " AND self.product IN :productList"
                    + " AND self.trackingNumber IS NOT NULL")
            .bind("stockLocation", stockLocation)
            .bind("productList", productList)
            .fetch();

    Map<Pair<Product, TrackingNumber>, InventoryLine> inventoryLineMap =
        getPairInventoryLineMap(inventoryLineList);

    if (detailsStockLocationLineList != null) {
      for (StockLocationLine detailsStockLocationLine : detailsStockLocationLineList) {
        Product product = detailsStockLocationLine.getProduct();
        TrackingNumber trackingNumber = detailsStockLocationLine.getTrackingNumber();
        InventoryLine matchingLine = inventoryLineMap.get(Pair.of(product, trackingNumber));
        BigDecimal realQty = matchingLine != null ? matchingLine.getRealQty() : BigDecimal.ZERO;
        updateLine(inventory, inventoryLineList, detailsStockLocationLine, realQty, product);
      }
    }
  }

  protected Map<Pair<Product, TrackingNumber>, InventoryLine> getPairInventoryLineMap(
      List<InventoryLine> inventoryLineList) {
    return inventoryLineList.stream()
        .filter(line -> line.getTrackingNumber() != null)
        .collect(
            Collectors.toMap(
                line -> Pair.of(line.getProduct(), line.getTrackingNumber()), line -> line));
  }

  protected void updateLine(
      Inventory inventory,
      List<InventoryLine> inventoryLineList,
      StockLocationLine stockLocationLine,
      BigDecimal realQty,
      Product product) {
    stockLocationLine.setLastInventoryRealQty(realQty);
    stockLocationLine.setLastInventoryDateT(
        inventory.getValidatedOn().atZone(ZoneId.systemDefault()));
    stockLocationLine.setRack(
        inventoryLineList.stream()
            .filter(line -> line.getProduct().equals(product))
            .map(InventoryLine::getRack)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(""));
  }
}
