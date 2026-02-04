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
package com.axelor.apps.stock.service.inventory;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.InventoryLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.db.JPA;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class InventoryStockLocationUpdateServiceImpl
    implements InventoryStockLocationUpdateService {

  private static final int DEFAULT_PRODUCT_PAGE_SIZE = 10;

  protected final StockLocationLineRepository stockLocationLineRepository;
  protected final InventoryLineRepository inventoryLineRepository;

  @Inject
  public InventoryStockLocationUpdateServiceImpl(
      StockLocationLineRepository stockLocationLineRepository,
      InventoryLineRepository inventoryLineRepository) {
    this.stockLocationLineRepository = stockLocationLineRepository;
    this.inventoryLineRepository = inventoryLineRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void storeLastInventoryData(Inventory inventory) {
    if (inventory == null) {
      return;
    }

    final Long inventoryId = inventory.getId();
    StockLocation stockLocation = inventory.getStockLocation();

    int offset = 0;
    final int productPageSize = DEFAULT_PRODUCT_PAGE_SIZE;
    List<Long> productIdPage;
    do {
      productIdPage = fetchDistinctProductIdsPage(inventoryId, offset, productPageSize);
      if (CollectionUtils.isEmpty(productIdPage)) {
        break;
      }

      List<InventoryLine> linesForPage = fetchInventoryLinesForProducts(inventoryId, productIdPage);

      List<StockLocationLine> stockLocationLines =
          fetchStockLocationLinesForProducts(stockLocation, productIdPage, /* details= */ false);
      List<StockLocationLine> detailsStockLocationLines =
          fetchStockLocationLinesForProducts(stockLocation, productIdPage, /* details= */ true);

      processPage(inventory, linesForPage, stockLocationLines, detailsStockLocationLines);

      JPA.flush();
      JPA.clear();

      inventory = JPA.find(Inventory.class, inventoryId);
      stockLocation = inventory.getStockLocation();

      offset += productPageSize;
    } while (productIdPage.size() == productPageSize);
  }

  /**
   * Fetch a page of distinct product ids for an inventory.
   *
   * @param inventoryId
   * @param offset
   * @param limit
   * @return list of product ids (can be empty)
   */
  protected List<Long> fetchDistinctProductIdsPage(Long inventoryId, int offset, int limit) {
    return JPA.em()
        .createQuery(
            "select distinct il.product.id "
                + "from InventoryLine il "
                + "where il.inventory.id = :invId "
                + "order by il.product.id",
            Long.class)
        .setParameter("invId", inventoryId)
        .setFirstResult(offset)
        .setMaxResults(limit)
        .getResultList();
  }

  /**
   * Fetch inventory lines for a set of product ids belonging to an inventory.
   *
   * @param inventoryId inventory id
   * @param productIds product ids to fetch
   * @return list of InventoryLine
   */
  protected List<InventoryLine> fetchInventoryLinesForProducts(
      Long inventoryId, List<Long> productIds) {
    if (CollectionUtils.isEmpty(productIds)) {
      return Collections.emptyList();
    }

    return inventoryLineRepository
        .all()
        .filter("self.inventory.id = :invId AND self.product.id IN :pids")
        .bind("invId", inventoryId)
        .bind("pids", productIds)
        .fetch();
  }

  /**
   * @param stockLocation stock location
   * @param productIds product ids
   * @param details
   * @return list of StockLocationLine
   */
  protected List<StockLocationLine> fetchStockLocationLinesForProducts(
      StockLocation stockLocation, List<Long> productIds, boolean details) {

    if (CollectionUtils.isEmpty(productIds)) {
      return Collections.emptyList();
    }

    StringBuilder filter = new StringBuilder();

    if (details) {
      filter.append("self.detailsStockLocation = :stockLocation ");
    } else {
      filter.append("self.stockLocation = :stockLocation ");
    }

    filter.append("AND self.product.id IN :pids ");

    if (details) {
      filter.append("AND self.trackingNumber IS NOT NULL");
    } else {
      filter.append("AND self.trackingNumber IS NULL");
    }

    return stockLocationLineRepository
        .all()
        .filter(filter.toString())
        .bind("stockLocation", stockLocation)
        .bind("pids", productIds)
        .fetch();
  }

  /**
   * Process one page: group inventory lines by product and update corresponding stockLocationLines
   *
   * @param inventory
   * @param linesForPage inventory lines that belong to products
   * @param stockLocationLines stock location lines without tracking
   * @param detailsStockLocationLines stock location lines with tracking
   */
  protected void processPage(
      Inventory inventory,
      List<InventoryLine> linesForPage,
      List<StockLocationLine> stockLocationLines,
      List<StockLocationLine> detailsStockLocationLines) {

    if (CollectionUtils.isEmpty(linesForPage)) {
      return;
    }

    Map<Long, List<InventoryLine>> invLinesByProduct =
        linesForPage.stream()
            .filter(Objects::nonNull)
            .filter(il -> il.getProduct() != null)
            .collect(Collectors.groupingBy(il -> il.getProduct().getId()));

    Map<Long, List<StockLocationLine>> nonTrackedByProduct =
        stockLocationLines.stream()
            .filter(Objects::nonNull)
            .filter(s -> s.getProduct() != null)
            .collect(Collectors.groupingBy(s -> s.getProduct().getId()));
    Map<ProductTrackingKey, StockLocationLine> trackedMap =
        buildTrackedMap(detailsStockLocationLines);

    for (Map.Entry<Long, List<InventoryLine>> entry : invLinesByProduct.entrySet()) {
      Long productId = entry.getKey();
      List<InventoryLine> inventoryLines = entry.getValue();
      if (CollectionUtils.isEmpty(inventoryLines)) {
        continue;
      }

      List<StockLocationLine> stockLocationLineList =
          nonTrackedByProduct.getOrDefault(productId, Collections.emptyList());
      if (CollectionUtils.isNotEmpty(stockLocationLineList)) {
        BigDecimal realQty = sumRealQty(inventoryLines);
        for (StockLocationLine sll : stockLocationLineList) {
          updateLine(inventory, inventoryLines, sll, realQty);
        }
      }

      updateTrackedLines(inventory, inventoryLines, trackedMap, productId);
    }
  }

  /**
   * Build a lookup map for tracked stockLocationLines keyed by (productId, trackingId).
   *
   * @param trackedSll tracked stock location lines
   * @return map keyed by ProductTrackingKey
   */
  protected Map<ProductTrackingKey, StockLocationLine> buildTrackedMap(
      List<StockLocationLine> trackedSll) {
    Map<ProductTrackingKey, StockLocationLine> map = new HashMap<>();
    if (CollectionUtils.isEmpty(trackedSll)) {
      return map;
    }
    for (StockLocationLine stockLocationLine : trackedSll) {
      Product product = stockLocationLine.getProduct();
      TrackingNumber trackingNumber = stockLocationLine.getTrackingNumber();
      if (product == null || trackingNumber == null) {
        continue;
      }
      map.put(ProductTrackingKey.of(product.getId(), trackingNumber.getId()), stockLocationLine);
    }
    return map;
  }

  /**
   * Update tracked stockLocationLines by matching each inventory line's tracking number and writing
   * its realQty to the matched SLL.
   */
  protected void updateTrackedLines(
      Inventory inventory,
      List<InventoryLine> inventoryLines,
      Map<ProductTrackingKey, StockLocationLine> trackedMap,
      Long productId) {

    for (InventoryLine il : inventoryLines) {
      if (il.getTrackingNumber() == null) {
        continue;
      }
      Long trackingId = il.getTrackingNumber().getId();
      ProductTrackingKey key = ProductTrackingKey.of(productId, trackingId);
      StockLocationLine stockLocationLine = trackedMap.get(key);
      if (stockLocationLine != null) {
        BigDecimal realQty = il.getRealQty() == null ? BigDecimal.ZERO : il.getRealQty();
        updateLine(inventory, inventoryLines, stockLocationLine, realQty);
      }
    }
  }

  protected BigDecimal sumRealQty(List<InventoryLine> lines) {
    return lines.stream()
        .map(InventoryLine::getRealQty)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * @param inventory
   * @param inventoryLineList
   * @param stockLocationLine
   * @param realQty
   */
  protected void updateLine(
      Inventory inventory,
      List<InventoryLine> inventoryLineList,
      StockLocationLine stockLocationLine,
      BigDecimal realQty) {

    if (stockLocationLine == null || inventory == null) {
      return;
    }

    stockLocationLine.setLastInventoryRealQty(realQty);
    stockLocationLine.setLastInventoryDateT(
        inventory.getValidatedOn().atZone(ZoneId.systemDefault()));

    String rack =
        inventoryLineList.stream()
            .filter(Objects::nonNull)
            .map(InventoryLine::getRack)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse("");
    stockLocationLine.setRack(rack);
    stockLocationLineRepository.save(stockLocationLine);
  }

  protected static final class ProductTrackingKey {
    private final Long productId;
    private final Long trackingId;

    private ProductTrackingKey(Long productId, Long trackingId) {
      this.productId = productId;
      this.trackingId = trackingId;
    }

    static ProductTrackingKey of(Long productId, Long trackingId) {
      return new ProductTrackingKey(productId, trackingId);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ProductTrackingKey)) {
        return false;
      }
      ProductTrackingKey that = (ProductTrackingKey) o;
      return Objects.equals(productId, that.productId)
          && Objects.equals(trackingId, that.trackingId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(productId, trackingId);
    }
  }
}
