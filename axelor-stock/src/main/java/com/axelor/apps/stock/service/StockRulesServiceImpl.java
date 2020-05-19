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
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockRules;
import com.axelor.apps.stock.db.repo.StockRulesRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class StockRulesServiceImpl implements StockRulesService {

  protected StockRulesRepository stockRuleRepo;

  @Inject
  public StockRulesServiceImpl(StockRulesRepository stockRuleRepo) {
    this.stockRuleRepo = stockRuleRepo;
  }

  public void generateOrder(
      Product product, BigDecimal qty, StockLocationLine stockLocationLine, int type)
      throws AxelorException {
    this.generatePurchaseOrder(product, qty, stockLocationLine, type);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void generatePurchaseOrder(
      Product product, BigDecimal qty, StockLocationLine stockLocationLine, int type)
      throws AxelorException {

    StockLocation stockLocation = stockLocationLine.getStockLocation();

    // TODO à supprimer après suppression des variantes
    if (stockLocation == null) {
      return;
    }

    StockRules stockRules =
        this.getStockRules(
            product, stockLocation, type, StockRulesRepository.USE_CASE_STOCK_CONTROL);

    if (stockRules == null) {
      return;
    }

    if (this.useMinStockRules(stockLocationLine, stockRules, qty, type)) {

      if (stockRules.getOrderAlertSelect() == StockRulesRepository.ORDER_ALERT_ALERT) {

        // TODO
      }
    }
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
   * @param qty the quantity of the stock move.
   * @param stockLocationLine
   * @param type current or future
   * @param stockRules
   * @param minReorderQty
   * @return the quantity to order
   */
  @Override
  public BigDecimal getQtyToOrder(
      BigDecimal qty,
      StockLocationLine stockLocationLine,
      int type,
      StockRules stockRules,
      BigDecimal minReorderQty) {
    minReorderQty = minReorderQty.max(stockRules.getReOrderQty());

    BigDecimal stockLocationLineQty =
        (type == StockRulesRepository.TYPE_CURRENT)
            ? stockLocationLine.getCurrentQty()
            : stockLocationLine.getFutureQty();

    // Get the quantity left in stock location line.
    BigDecimal qtyToOrder = stockLocationLineQty.subtract(qty);

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
  public BigDecimal getQtyToOrder(
      BigDecimal qty, StockLocationLine stockLocationLine, int type, StockRules stockRules) {
    return getQtyToOrder(qty, stockLocationLine, type, stockRules, BigDecimal.ZERO);
  }

  @Override
  public boolean useMinStockRules(
      StockLocationLine stockLocationLine, StockRules stockRules, BigDecimal qty, int type) {

    BigDecimal currentQty = stockLocationLine.getCurrentQty();
    BigDecimal futureQty = stockLocationLine.getFutureQty();

    BigDecimal minQty = stockRules.getMinQty();

    if (type == StockRulesRepository.TYPE_CURRENT) {

      if (currentQty.compareTo(minQty) >= 0 && (currentQty.subtract(qty)).compareTo(minQty) == -1) {
        return true;
      }

    } else if (type == StockRulesRepository.TYPE_FUTURE) {

      if (futureQty.compareTo(minQty) >= 0 && (futureQty.subtract(qty)).compareTo(minQty) == -1) {
        return true;
      }
    }
    return false;
  }

  @Override
  public StockRules getStockRules(
      Product product, StockLocation stockLocation, int type, int useCase) {

    if (useCase == StockRulesRepository.USE_CASE_USED_FOR_MRP) {
      return stockRuleRepo
          .all()
          .filter(
              "self.product = ?1 AND self.stockLocation = ?2 AND self.useCaseSelect = ?3",
              product,
              stockLocation,
              useCase)
          .fetchOne();
    } else if (useCase == StockRulesRepository.USE_CASE_STOCK_CONTROL) {
      return stockRuleRepo
          .all()
          .filter(
              "self.product = ?1 AND self.stockLocation = ?2 AND self.useCaseSelect = ?3 AND self.typeSelect = ?4",
              product,
              stockLocation,
              useCase,
              type)
          .fetchOne();
    } else {
      return null;
    }

    // TODO , plusieurs régles min de stock par produit (achat a 500 et production a 100)...

  }
}
