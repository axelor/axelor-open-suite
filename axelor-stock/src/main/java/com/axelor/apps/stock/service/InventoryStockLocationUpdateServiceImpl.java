/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
    List<StockLocation> stockLocationList =
        inventoryLineList.stream()
            .map(InventoryLine::getStockLocation)
            .distinct()
            .collect(Collectors.toList());

    updateStockLocationLines(inventory, stockLocationList, productList, inventoryLineList);
    updateDetailsStockLocationLines(inventory, stockLocationList, productList, inventoryLineList);
  }

  protected void updateStockLocationLines(
      Inventory inventory,
      List<StockLocation> stockLocationList,
      List<Product> productList,
      List<InventoryLine> inventoryLineList) {
    List<StockLocationLine> stockLocationLineList =
        stockLocationLineRepository
            .all()
            .filter(
                "self.stockLocation IN :stockLocationList"
                    + " AND self.product IN :productList"
                    + " AND self.trackingNumber IS NULL")
            .bind("stockLocationList", stockLocationList)
            .bind("productList", productList)
            .fetch();

    Map<Pair<Product, StockLocation>, BigDecimal> productStockLocationToQtyMap =
        getProductStockLocationQtyMap(inventoryLineList);

    for (StockLocationLine stockLocationLine : stockLocationLineList) {
      Product product = stockLocationLine.getProduct();
      StockLocation stockLocation = stockLocationLine.getStockLocation();
      BigDecimal realQty = productStockLocationToQtyMap.get(Pair.of(product, stockLocation));
      if (realQty == null) {
        continue;
      }
      updateLine(inventory, inventoryLineList, stockLocationLine, realQty, product, stockLocation);
    }
  }

  protected Map<Pair<Product, StockLocation>, BigDecimal> getProductStockLocationQtyMap(
      List<InventoryLine> inventoryLineList) {
    return inventoryLineList.stream()
        .filter(line -> line.getTrackingNumber() == null)
        .collect(
            Collectors.groupingBy(
                line -> Pair.of(line.getProduct(), line.getStockLocation()),
                Collectors.mapping(
                    InventoryLine::getRealQty,
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
  }

  protected void updateDetailsStockLocationLines(
      Inventory inventory,
      List<StockLocation> stockLocationList,
      List<Product> productList,
      List<InventoryLine> inventoryLineList) {
    List<StockLocationLine> detailsStockLocationLineList =
        stockLocationLineRepository
            .all()
            .filter(
                "self.detailsStockLocation IN :stockLocationList"
                    + " AND self.product IN :productList"
                    + " AND self.trackingNumber IS NOT NULL")
            .bind("stockLocationList", stockLocationList)
            .bind("productList", productList)
            .fetch();

    Map<Pair<Pair<Product, TrackingNumber>, StockLocation>, InventoryLine> inventoryLineMap =
        getProductTrackingStockLocationInventoryLineMap(inventoryLineList);

    if (detailsStockLocationLineList != null) {
      for (StockLocationLine detailsStockLocationLine : detailsStockLocationLineList) {
        Product product = detailsStockLocationLine.getProduct();
        TrackingNumber trackingNumber = detailsStockLocationLine.getTrackingNumber();
        StockLocation detailsStockLocation = detailsStockLocationLine.getDetailsStockLocation();
        InventoryLine matchingLine =
            inventoryLineMap.get(Pair.of(Pair.of(product, trackingNumber), detailsStockLocation));
        if (matchingLine == null) {
          continue;
        }
        BigDecimal realQty = matchingLine.getRealQty();
        updateLine(
            inventory,
            inventoryLineList,
            detailsStockLocationLine,
            realQty,
            product,
            detailsStockLocation);
      }
    }
  }

  protected Map<Pair<Pair<Product, TrackingNumber>, StockLocation>, InventoryLine>
      getProductTrackingStockLocationInventoryLineMap(List<InventoryLine> inventoryLineList) {
    return inventoryLineList.stream()
        .filter(line -> line.getTrackingNumber() != null)
        .collect(
            Collectors.toMap(
                line ->
                    Pair.of(
                        Pair.of(line.getProduct(), line.getTrackingNumber()),
                        line.getStockLocation()),
                line -> line));
  }

  protected void updateLine(
      Inventory inventory,
      List<InventoryLine> inventoryLineList,
      StockLocationLine stockLocationLine,
      BigDecimal realQty,
      Product product,
      StockLocation stockLocation) {
    stockLocationLine.setLastInventoryRealQty(realQty);
    stockLocationLine.setLastInventoryDateT(
        inventory.getValidatedOn().atZone(ZoneId.systemDefault()));
    stockLocationLine.setRack(
        inventoryLineList.stream()
            .filter(
                line ->
                    line.getProduct().equals(product)
                        && Objects.equals(line.getStockLocation(), stockLocation))
            .map(InventoryLine::getRack)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(""));
  }
}
