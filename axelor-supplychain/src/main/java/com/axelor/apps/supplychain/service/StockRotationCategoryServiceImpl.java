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
import com.axelor.apps.supplychain.db.StockRotationCategory;
import com.axelor.apps.supplychain.db.repo.StockRotationCategoryRepository;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptBindings;
import com.axelor.script.ScriptHelper;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockRotationCategoryServiceImpl implements StockRotationCategoryService {

  private static final Logger log = LoggerFactory.getLogger(StockRotationCategoryServiceImpl.class);

  protected final StockRotationCategoryRepository stockRotationCategoryRepository;
  protected final ProductStockAnalysisService productStockAnalysisService;
  protected final ProductRepository productRepository;

  @Inject
  public StockRotationCategoryServiceImpl(
      StockRotationCategoryRepository stockRotationCategoryRepository,
      ProductStockAnalysisService productStockAnalysisService,
      ProductRepository productRepository) {
    this.stockRotationCategoryRepository = stockRotationCategoryRepository;
    this.productStockAnalysisService = productStockAnalysisService;
    this.productRepository = productRepository;
  }

  @Override
  public void checkFormula(StockRotationCategory stockRotationCategory) throws ScriptException {
    String formula = stockRotationCategory.getFormula();
    if (formula == null || formula.isBlank()) {
      return;
    }

    Map<String, Object> context = new HashMap<>();
    context.put(VAR_COVERAGE_IN_DAYS, BigDecimal.ONE);
    context.put(VAR_SALES_LAST_12_MONTHS, BigDecimal.ONE);
    context.put(VAR_CURRENT_STOCK, BigDecimal.ONE);
    context.put(VAR_STOCK_6_MONTHS_AGO, BigDecimal.ONE);

    try {
      ScriptHelper scriptHelper = new GroovyScriptHelper(new ScriptBindings(context));
      scriptHelper.eval(formula);
    } catch (Exception e) {
      throw new ScriptException(e.getMessage());
    }
  }

  @Override
  public boolean evaluateFormula(StockRotationCategory stockRotationCategory, Product product) {
    String formula = stockRotationCategory.getFormula();
    if (formula == null || formula.isBlank() || product == null) {
      return false;
    }

    Map<String, Object> context = buildContext(product);

    try {
      ScriptHelper scriptHelper = new GroovyScriptHelper(new ScriptBindings(context));
      Object result = scriptHelper.eval(formula);

      return Boolean.TRUE.equals(result);
    } catch (Exception e) {
      log.warn(
          "Formula evaluation failed for StockRotationCategory {} on product {}: {}",
          stockRotationCategory.getCode(),
          product.getCode(),
          e.getMessage());
      return false;
    }
  }

  @Override
  public StockRotationCategory findMatchingCategory(Product product) {
    if (product == null) {
      return null;
    }
    List<StockRotationCategory> categories =
        stockRotationCategoryRepository.all().order("sequence").order("id").fetch();
    for (StockRotationCategory category : categories) {
      if (evaluateFormula(category, product)) {
        return category;
      }
    }
    return null;
  }

  @Override
  @Transactional
  public void assignStockRotationCategory(Product product) {
    if (product == null || !Boolean.TRUE.equals(product.getAutoAssignStockRotationCategory())) {
      return;
    }
    StockRotationCategory match = findMatchingCategory(product);
    if (match == null) {
      return;
    }
    if (match.equals(product.getStockRotationCategory())) {
      return;
    }
    product.setStockRotationCategory(match);
    productRepository.save(product);
  }

  protected Map<String, Object> buildContext(Product product) {
    Map<String, Object> context = new HashMap<>();
    BigDecimal coverageInDays = productStockAnalysisService.computeSlowOrFastMover(product);
    BigDecimal salesLast12Months = productStockAnalysisService.computeSales(product);
    BigDecimal currentStock = productStockAnalysisService.computeStocks(product);
    BigDecimal stock6MonthsAgo = productStockAnalysisService.computeStocks6MonthsAgo(product);
    context.put(VAR_COVERAGE_IN_DAYS, coverageInDays != null ? coverageInDays : BigDecimal.ZERO);
    context.put(
        VAR_SALES_LAST_12_MONTHS, salesLast12Months != null ? salesLast12Months : BigDecimal.ZERO);
    context.put(VAR_CURRENT_STOCK, currentStock != null ? currentStock : BigDecimal.ZERO);
    context.put(
        VAR_STOCK_6_MONTHS_AGO, stock6MonthsAgo != null ? stock6MonthsAgo : BigDecimal.ZERO);
    return context;
  }
}
