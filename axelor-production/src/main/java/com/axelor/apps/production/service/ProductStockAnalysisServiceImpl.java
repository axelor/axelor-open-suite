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
package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.repo.StockHistoryLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ProductStockAnalysisServiceImpl implements ProductStockAnalysisService {

  protected final AppBaseService appBaseService;
  protected final ProductRepository productRepository;
  protected final StockHistoryLineRepository stockHistoryLineRepository;
  protected final StockLocationLineRepository stockLocationLineRepository;

  @Inject
  public ProductStockAnalysisServiceImpl(
      AppBaseService appBaseService,
      ProductRepository productRepository,
      StockHistoryLineRepository stockHistoryLineRepository,
      StockLocationLineRepository stockLocationLineRepository) {
    this.appBaseService = appBaseService;
    this.productRepository = productRepository;
    this.stockHistoryLineRepository = stockHistoryLineRepository;
    this.stockLocationLineRepository = stockLocationLineRepository;
  }

  @Override
  public BigDecimal computeSales(Product product) {
    LocalDate todayDate = appBaseService.getTodayDate(null);
    LocalDate fromDate = todayDate.minusYears(1).withDayOfMonth(1);

    // Sum of outgoing quantities over the last 12 months from StockHistoryLine
    List<Map> results =
        stockHistoryLineRepository
            .all()
            .filter("self.product = :product AND self.period.fromDate >= :fromDate")
            .bind("product", product)
            .bind("fromDate", fromDate)
            .select("SUM(self.sumOutQtyPeriod)")
            .fetch(0, 0);

    BigDecimal sum = getBigDecimal(results);
    if (sum != null) {
      return sum;
    }

    return BigDecimal.ZERO;
  }

  @Override
  public BigDecimal computeStocks(Product product) {
    // Sum of current quantities from StockLocationLine
    List<Map> results =
        stockLocationLineRepository
            .all()
            .filter("self.product = :product")
            .bind("product", product)
            .select("SUM(self.currentQty)")
            .fetch(0, 0);

    BigDecimal sum = getBigDecimal(results);
    if (sum != null) {
      return sum;
    }

    return BigDecimal.ZERO;
  }

  protected BigDecimal getBigDecimal(List<Map> results) {
    if (results != null && !results.isEmpty() && results.get(0) != null) {
      Object sum = results.get(0);
      if (sum instanceof BigDecimal) {
        return (BigDecimal) sum;
      } else if (sum instanceof Number) {
        return BigDecimal.valueOf(((Number) sum).doubleValue());
      }
    }
    return null;
  }

  @Override
  public BigDecimal computeStocks6MonthsAgo(Product product) {
    // This is more complex as we need historical data
    // We'll use StockHistoryLine to get quantities from 6-12 months ago period
    LocalDate todayDate = appBaseService.getTodayDate(null);
    LocalDate fromDate = todayDate.minusMonths(12);
    LocalDate toDate = todayDate.minusMonths(6);

    List<Map> results =
        stockHistoryLineRepository
            .all()
            .filter(
                "self.product = :product AND self.period.fromDate >= :fromDate AND self.period.toDate <= :toDate")
            .bind("product", product)
            .bind("fromDate", fromDate)
            .bind("toDate", toDate)
            .select("SUM(self.sumIncQtyPeriod - self.sumOutQtyPeriod)")
            .fetch(0, 0);

    if (results != null && !results.isEmpty() && results.get(0) != null) {
      Object sum = results.get(0);
      BigDecimal netMovement =
          (sum instanceof BigDecimal)
              ? (BigDecimal) sum
              : BigDecimal.valueOf(((Number) sum).doubleValue());

      // This is an approximation - ideally we'd have snapshot data
      BigDecimal currentStock = computeStocks(product);
      return currentStock.subtract(netMovement);
    }

    return BigDecimal.ZERO;
  }

  @Override
  public BigDecimal computeSlowOrFastMover(Product product) {
    BigDecimal sales = computeSales(product);
    BigDecimal stocks = computeStocks(product);

    if (sales != null && stocks != null && sales.signum() != 0) {
      return stocks.divide(
          sales.divide(BigDecimal.valueOf(360), 10, RoundingMode.HALF_EVEN),
          10,
          RoundingMode.HALF_EVEN);
    }

    return BigDecimal.ZERO;
  }
}
