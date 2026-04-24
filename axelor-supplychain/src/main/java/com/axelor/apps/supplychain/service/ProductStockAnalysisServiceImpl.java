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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.repo.StockHistoryLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.db.JPA;
import jakarta.inject.Inject;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

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
    if (product == null || product.getId() == null) {
      return BigDecimal.ZERO;
    }
    LocalDate todayDate = appBaseService.getTodayDate(null);
    LocalDate fromDate = todayDate.minusYears(1).withDayOfMonth(1);

    String jpql =
        "SELECT COALESCE(SUM(self.sumOutQtyPeriod), 0) FROM StockHistoryLine self "
            + "WHERE self.product.id = :productId AND self.period.fromDate >= :fromDate";

    Query query = JPA.em().createQuery(jpql);
    query.setParameter("productId", product.getId());
    query.setParameter("fromDate", fromDate);

    return toBigDecimal(query.getSingleResult());
  }

  @Override
  public BigDecimal computeStocks(Product product) {
    if (product == null || product.getId() == null) {
      return BigDecimal.ZERO;
    }
    String jpql =
        "SELECT COALESCE(SUM(self.currentQty), 0) FROM StockLocationLine self "
            + "WHERE self.product.id = :productId "
            + "AND self.stockLocation.typeSelect != :virtualType";

    Query query = JPA.em().createQuery(jpql);
    query.setParameter("productId", product.getId());
    query.setParameter("virtualType", StockLocationRepository.TYPE_VIRTUAL);

    return toBigDecimal(query.getSingleResult());
  }

  @Override
  public BigDecimal computeStocks6MonthsAgo(Product product) {
    if (product == null || product.getId() == null) {
      return BigDecimal.ZERO;
    }
    LocalDate todayDate = appBaseService.getTodayDate(null);
    LocalDate fromDate = todayDate.minusMonths(12);
    LocalDate toDate = todayDate.minusMonths(6);

    String jpql =
        "SELECT COALESCE(SUM(self.sumIncQtyPeriod - self.sumOutQtyPeriod), 0) "
            + "FROM StockHistoryLine self "
            + "WHERE self.product.id = :productId "
            + "AND self.period.fromDate >= :fromDate "
            + "AND self.period.toDate <= :toDate";

    Query query = JPA.em().createQuery(jpql);
    query.setParameter("productId", product.getId());
    query.setParameter("fromDate", fromDate);
    query.setParameter("toDate", toDate);

    BigDecimal netMovement = toBigDecimal(query.getSingleResult());
    return computeStocks(product).subtract(netMovement);
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

  protected BigDecimal toBigDecimal(Object value) {
    if (value == null) {
      return BigDecimal.ZERO;
    }
    if (value instanceof BigDecimal) {
      return (BigDecimal) value;
    }
    if (value instanceof Number) {
      return BigDecimal.valueOf(((Number) value).doubleValue());
    }
    return BigDecimal.ZERO;
  }
}
