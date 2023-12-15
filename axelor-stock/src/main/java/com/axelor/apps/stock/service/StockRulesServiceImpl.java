/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockRules;
import com.axelor.apps.stock.db.repo.StockRulesRepository;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

public class StockRulesServiceImpl implements StockRulesService {

  protected StockRulesRepository stockRuleRepo;
  protected StockLocationService stockLocationService;

  @Inject
  public StockRulesServiceImpl(
      StockRulesRepository stockRuleRepo, StockLocationService stockLocationService) {
    this.stockRuleRepo = stockRuleRepo;
    this.stockLocationService = stockLocationService;
  }

  /**
   * Called on creating a new purchase or production order. Takes into account the reorder qty and
   * the min/ideal quantity in stock rules.
   *
   * <p>with L the quantity that will be left in the stock location, M the min/ideal qty, R the
   * reorder quantity and O the quantity to order :
   *
   * <p>O = max(R, M - L)
   *
   * @param stockLocationLine
   * @param stockRules
   * @param minReorderQty
   * @return the quantity to order
   */
  @Override
  public BigDecimal getQtyToOrder(
      StockLocationLine stockLocationLine, StockRules stockRules, BigDecimal minReorderQty) {
    minReorderQty = minReorderQty.max(stockRules.getReOrderQty());

    BigDecimal stockLocationLineQty =
        (stockRules.getTypeSelect() == StockRulesRepository.TYPE_CURRENT)
            ? stockLocationLine.getCurrentQty()
            : stockLocationLine.getFutureQty();

    // Get the quantity left in stock location line.
    BigDecimal qtyToOrder = stockLocationLineQty;

    // The quantity to reorder is the difference between the min/ideal
    // quantity and the quantity left in the stock location.
    BigDecimal targetQty =
        stockRules.getUseIdealQty() ? stockRules.getIdealQty() : stockRules.getMinQty();
    qtyToOrder = targetQty.subtract(qtyToOrder);

    // If the quantity we need to order is less than the reorder quantity,
    // we must choose the reorder quantity instead.
    qtyToOrder = qtyToOrder.max(minReorderQty);

    // Limit the quantity to order in order to not exceed to max quantity
    // rule.
    if (stockRules.getUseMaxQty()) {
      BigDecimal maxQtyToReorder = stockRules.getMaxQty().subtract(stockLocationLineQty);
      qtyToOrder = qtyToOrder.min(maxQtyToReorder);
    }

    return qtyToOrder;
  }

  @Override
  public BigDecimal getQtyToOrder(StockLocationLine stockLocationLine, StockRules stockRules) {
    return getQtyToOrder(stockLocationLine, stockRules, BigDecimal.ZERO);
  }

  @Override
  public boolean useMinStockRules(
      StockLocationLine stockLocationLine, StockRules stockRules, int type) {

    Pair<BigDecimal, BigDecimal> qtyPair =
        getCurrentQtySumAndFutureQtySumOfProductInStockLocationAndItsContainedStockLocations(
            stockRules.getStockLocation(), stockRules.getProduct());
    BigDecimal currentQty = qtyPair.getLeft();
    BigDecimal futureQty = qtyPair.getRight();

    BigDecimal minQty = stockRules.getMinQty();

    if (type == StockRulesRepository.TYPE_CURRENT) {
      if (currentQty.compareTo(minQty) < 0) {
        return true;
      }
    } else if (type == StockRulesRepository.TYPE_FUTURE) {
      if (futureQty.compareTo(minQty) < 0) {
        return true;
      }
    }
    return false;
  }

  @Override
  public StockRules getStockRules(
      Product product, StockLocation stockLocation, int type, int useCase) {
    Set<Long> stockLocationIds =
        stockLocationService.getLocationAndAllParentLocationsIdsOrderedFromTheClosestToTheFurthest(
            stockLocation);

    StockRules stockRules = null;
    if (useCase == StockRulesRepository.USE_CASE_USED_FOR_MRP) {
      String filter =
          "self.product = :product AND self.stockLocation.id = :stockLocationId AND self.useCaseSelect = :useCase";
      /* We use this for statement and not a query with `in List` in it so that we can get the
      first stockRule that is the closest to the stockLocation given in the parameter.*/
      for (Long stockLocationId : stockLocationIds) {
        stockRules =
            stockRuleRepo
                .all()
                .filter(filter)
                .bind("product", product)
                .bind("stockLocationId", stockLocationId)
                .bind("useCase", useCase)
                .fetchOne();
        if (stockRules != null) {
          break;
        }
      }
    } else if (useCase == StockRulesRepository.USE_CASE_STOCK_CONTROL) {
      String filter =
          "self.product = :product AND self.stockLocation.id = :stockLocationId AND self.useCaseSelect = :useCase AND self.typeSelect = :type";
      /* We use this for statement and not a query with `in List` in it so that we can get the
      first stockRule that is the closest to the stockLocation given in the parameter.*/
      for (Long stockLocationId : stockLocationIds) {
        stockRules =
            stockRuleRepo
                .all()
                .filter(filter)
                .bind("product", product)
                .bind("stockLocationId", stockLocationId)
                .bind("useCase", useCase)
                .bind("type", type)
                .fetchOne();
        if (stockRules != null) {
          break;
        }
      }
    }
    return stockRules;
  }

  /**
   * Gets the sum of all currentQty and all futureQty of a product in a stockLocation and all it's
   * contained stockLocations.
   *
   * @param stockLocation
   * @param product
   * @return
   */
  @SuppressWarnings("unchecked")
  protected Pair<BigDecimal, BigDecimal>
      getCurrentQtySumAndFutureQtySumOfProductInStockLocationAndItsContainedStockLocations(
          StockLocation stockLocation, Product product) {
    Set<Long> containedStockLocationIds =
        stockLocationService.getContentStockLocationIds(stockLocation);

    List<Object[]> resultList =
        JPA.em()
            .createQuery(
                "SELECT SUM(sll.currentQty),SUM(sll.futureQty) FROM StockLocationLine sll "
                    + " WHERE sll.product = :product"
                    + " AND sll.stockLocation.id IN :containedStockLocationIds"
                    + " AND ((sll.stockLocation.includeOutOfStock = false AND (sll.currentQty != 0 OR sll.futureQty != 0)) OR (sll.stockLocation.includeOutOfStock = true))"
                    + " AND ((sll.stockLocation.typeSelect = 3) OR (sll.stockLocation.typeSelect != 3 AND ((sll.stockLocation.includeVirtualSubLocation = true) OR (sll.stockLocation.includeVirtualSubLocation = false AND sll.stockLocation.typeSelect != 3))))")
            .setParameter("containedStockLocationIds", containedStockLocationIds)
            .setParameter("product", product)
            .getResultList();
    if (CollectionUtils.isEmpty(resultList)) {
      return null;
    }
    Object[] resultElement = resultList.get(0);

    BigDecimal currentQty =
        resultElement[0] != null ? (BigDecimal) resultElement[0] : BigDecimal.ZERO;
    BigDecimal futureQty =
        resultElement[1] != null ? (BigDecimal) resultElement[1] : BigDecimal.ZERO;
    return Pair.of(currentQty, futureQty);
  }
}
