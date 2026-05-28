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
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
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

    int offset = 0;
    final int productPageSize = DEFAULT_PRODUCT_PAGE_SIZE;
    List<Long> productIdPage;
    do {
      productIdPage = fetchDistinctProductIdsPage(inventoryId, offset, productPageSize);
      if (CollectionUtils.isEmpty(productIdPage)) {
        break;
      }

      List<InventoryLine> linesForPage = fetchInventoryLinesForProducts(inventoryId, productIdPage);

      List<StockLocation> locationsForPage =
          linesForPage.stream()
              .map(InventoryLine::getStockLocation)
              .filter(Objects::nonNull)
              .distinct()
              .collect(Collectors.toList());

      List<StockLocationLine> stockLocationLines =
          fetchStockLocationLinesForProducts(locationsForPage, productIdPage, /* details= */ false);
      List<StockLocationLine> detailsStockLocationLines =
          fetchStockLocationLinesForProducts(locationsForPage, productIdPage, /* details= */ true);

      processPage(inventory, linesForPage, stockLocationLines, detailsStockLocationLines);

      JPA.flush();
      JPA.clear();

      inventory = JPA.find(Inventory.class, inventoryId);

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
   * @param stockLocations stock locations
   * @param productIds product ids
   * @param details
   * @return list of StockLocationLine
   */
  protected List<StockLocationLine> fetchStockLocationLinesForProducts(
      List<StockLocation> stockLocations, List<Long> productIds, boolean details) {

    if (CollectionUtils.isEmpty(productIds) || CollectionUtils.isEmpty(stockLocations)) {
      return Collections.emptyList();
    }

    StringBuilder filter = new StringBuilder();

    if (details) {
      filter.append("self.detailsStockLocation IN :stockLocations ");
    } else {
      filter.append("self.stockLocation IN :stockLocations ");
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
        .bind("stockLocations", stockLocations)
        .bind("pids", productIds)
        .fetch();
  }

  /**
   * Process one page: group inventory lines by (product, location) and update corresponding
   * stockLocationLines
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

    Map<ProductLocationKey, List<InventoryLine>> invLinesByProductAndLocation =
        linesForPage.stream()
            .filter(Objects::nonNull)
            .filter(il -> il.getProduct() != null && il.getStockLocation() != null)
            .collect(
                Collectors.groupingBy(
                    il ->
                        ProductLocationKey.of(
                            il.getProduct().getId(), il.getStockLocation().getId())));

    Map<ProductLocationKey, List<StockLocationLine>> nonTrackedMap =
        stockLocationLines.stream()
            .filter(Objects::nonNull)
            .filter(s -> s.getProduct() != null && s.getStockLocation() != null)
            .collect(
                Collectors.groupingBy(
                    s ->
                        ProductLocationKey.of(
                            s.getProduct().getId(), s.getStockLocation().getId())));
    Map<ProductLocationTrackingKey, StockLocationLine> trackedMap =
        buildTrackedMap(detailsStockLocationLines);

    for (Map.Entry<ProductLocationKey, List<InventoryLine>> entry :
        invLinesByProductAndLocation.entrySet()) {
      ProductLocationKey key = entry.getKey();
      List<InventoryLine> inventoryLines = entry.getValue();
      if (CollectionUtils.isEmpty(inventoryLines)) {
        continue;
      }

      List<StockLocationLine> stockLocationLineList =
          nonTrackedMap.getOrDefault(key, Collections.emptyList());
      if (CollectionUtils.isNotEmpty(stockLocationLineList)) {
        BigDecimal realQty = sumRealQty(inventoryLines);
        for (StockLocationLine sll : stockLocationLineList) {
          updateLine(inventory, inventoryLines, sll, realQty);
        }
      }

      updateTrackedLines(inventory, inventoryLines, trackedMap, key.productId);
    }
  }

  /**
   * Build a lookup map for tracked stockLocationLines keyed by (productId, trackingId, locationId).
   *
   * @param trackedSll tracked stock location lines
   * @return map keyed by ProductLocationTrackingKey
   */
  protected Map<ProductLocationTrackingKey, StockLocationLine> buildTrackedMap(
      List<StockLocationLine> trackedSll) {
    Map<ProductLocationTrackingKey, StockLocationLine> map = new HashMap<>();
    if (CollectionUtils.isEmpty(trackedSll)) {
      return map;
    }
    for (StockLocationLine stockLocationLine : trackedSll) {
      Product product = stockLocationLine.getProduct();
      TrackingNumber trackingNumber = stockLocationLine.getTrackingNumber();
      StockLocation location = stockLocationLine.getDetailsStockLocation();
      if (product == null || trackingNumber == null || location == null) {
        continue;
      }
      map.put(
          ProductLocationTrackingKey.of(product.getId(), trackingNumber.getId(), location.getId()),
          stockLocationLine);
    }
    return map;
  }

  /**
   * Update tracked stockLocationLines by matching each inventory line's tracking number and
   * location, writing its realQty to the matched SLL.
   */
  protected void updateTrackedLines(
      Inventory inventory,
      List<InventoryLine> inventoryLines,
      Map<ProductLocationTrackingKey, StockLocationLine> trackedMap,
      Long productId) {

    for (InventoryLine il : inventoryLines) {
      if (il.getTrackingNumber() == null || il.getStockLocation() == null) {
        continue;
      }
      Long trackingId = il.getTrackingNumber().getId();
      Long locationId = il.getStockLocation().getId();
      ProductLocationTrackingKey key =
          ProductLocationTrackingKey.of(productId, trackingId, locationId);
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

  protected static final class ProductLocationKey {
    final Long productId;
    final Long locationId;

    private ProductLocationKey(Long productId, Long locationId) {
      this.productId = productId;
      this.locationId = locationId;
    }

    static ProductLocationKey of(Long productId, Long locationId) {
      return new ProductLocationKey(productId, locationId);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ProductLocationKey)) {
        return false;
      }
      ProductLocationKey that = (ProductLocationKey) o;
      return Objects.equals(productId, that.productId)
          && Objects.equals(locationId, that.locationId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(productId, locationId);
    }
  }

  protected static final class ProductLocationTrackingKey {
    private final Long productId;
    private final Long trackingId;
    private final Long locationId;

    private ProductLocationTrackingKey(Long productId, Long trackingId, Long locationId) {
      this.productId = productId;
      this.trackingId = trackingId;
      this.locationId = locationId;
    }

    static ProductLocationTrackingKey of(Long productId, Long trackingId, Long locationId) {
      return new ProductLocationTrackingKey(productId, trackingId, locationId);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ProductLocationTrackingKey)) {
        return false;
      }
      ProductLocationTrackingKey that = (ProductLocationTrackingKey) o;
      return Objects.equals(productId, that.productId)
          && Objects.equals(trackingId, that.trackingId)
          && Objects.equals(locationId, that.locationId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(productId, trackingId, locationId);
    }
  }
}
