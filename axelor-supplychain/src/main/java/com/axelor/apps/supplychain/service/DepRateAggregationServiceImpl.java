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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.service.ProductCategoryService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.supplychain.db.DepreciationRateConfig;
import com.axelor.apps.supplychain.db.repo.DepreciationRateConfigRepository;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class DepRateAggregationServiceImpl implements DepRateAggregationService {

  protected static final BigDecimal HUNDRED = new BigDecimal("100");

  protected final DepreciationRateConfigRepository depreciationRateConfigRepository;
  protected final ProductCategoryService productCategoryService;
  protected final AppBaseService appBaseService;

  @Inject
  public DepRateAggregationServiceImpl(
      DepreciationRateConfigRepository depreciationRateConfigRepository,
      ProductCategoryService productCategoryService,
      AppBaseService appBaseService) {
    this.depreciationRateConfigRepository = depreciationRateConfigRepository;
    this.productCategoryService = productCategoryService;
    this.appBaseService = appBaseService;
  }

  @Override
  public AggregatedRates aggregate(Product product) throws AxelorException {
    List<DepreciationRateConfig> configs = findApplicableConfigs(product);
    if (configs.isEmpty()) {
      return new AggregatedRates(
          null, null, BigDecimal.ZERO, DepreciationRateConfigRepository.TYPE_DEPRECIATION);
    }

    BigDecimal multiplier = BigDecimal.ONE;
    for (DepreciationRateConfig config : configs) {
      multiplier = multiplier.multiply(signedRateFactor(config));
    }
    BigDecimal signedRate = multiplier.subtract(BigDecimal.ONE);
    int typeSelect =
        signedRate.signum() < 0
            ? DepreciationRateConfigRepository.TYPE_DEPRECIATION
            : DepreciationRateConfigRepository.TYPE_VALORIZATION;
    BigDecimal costToApply =
        signedRate
            .abs()
            .multiply(HUNDRED)
            .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);

    BigDecimal minRate = null;
    BigDecimal maxRate = null;
    for (DepreciationRateConfig config : configs) {
      if (config.getTypeSelect() != typeSelect) {
        continue;
      }
      if (config.getMinRate() != null) {
        minRate = (minRate == null) ? config.getMinRate() : minRate.max(config.getMinRate());
      }
      if (config.getMaxRate() != null) {
        maxRate = (maxRate == null) ? config.getMaxRate() : maxRate.min(config.getMaxRate());
      }
    }

    return new AggregatedRates(minRate, maxRate, costToApply, typeSelect);
  }

  protected List<DepreciationRateConfig> findApplicableConfigs(Product product)
      throws AxelorException {
    List<DepreciationRateConfig> configs = new ArrayList<>();
    addIfPresent(configs, findConfigBy("product", product));
    addIfPresent(configs, findMostSpecificCategoryConfig(product.getProductCategory()));
    addIfPresent(configs, findConfigBy("productFamily", product.getProductFamily()));
    addIfPresent(
        configs, findConfigBy("stockRotationCategory", product.getStockRotationCategory()));
    return configs;
  }

  /**
   * Picks the most specific config in the category chain. With the new multi-dimensional
   * aggregation model only one category contributes per product; resolving leaf-most preserves
   * intent (a config on a sub-category overrides one on its parent). Uses {@link
   * ProductCategoryService#fetchParentCategoryList} for cycle-safe traversal.
   */
  protected DepreciationRateConfig findMostSpecificCategoryConfig(ProductCategory category)
      throws AxelorException {
    if (category == null) {
      return null;
    }
    DepreciationRateConfig config = findConfigBy("productCategory", category);
    if (config != null) {
      return config;
    }
    for (ProductCategory parent : productCategoryService.fetchParentCategoryList(category)) {
      config = findConfigBy("productCategory", parent);
      if (config != null) {
        return config;
      }
    }
    return null;
  }

  protected DepreciationRateConfig findConfigBy(String field, Object value) {
    if (value == null) {
      return null;
    }
    return depreciationRateConfigRepository
        .all()
        .filter("self." + field + " = :value")
        .bind("value", value)
        .fetchOne();
  }

  protected void addIfPresent(List<DepreciationRateConfig> configs, DepreciationRateConfig config) {
    if (config != null) {
      configs.add(config);
    }
  }

  protected BigDecimal signedRateFactor(DepreciationRateConfig config) {
    BigDecimal rate = config.getDefaultRate() != null ? config.getDefaultRate() : BigDecimal.ZERO;
    BigDecimal rateFactor =
        rate.divide(HUNDRED, appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
    return config.getTypeSelect() == DepreciationRateConfigRepository.TYPE_VALORIZATION
        ? BigDecimal.ONE.add(rateFactor)
        : BigDecimal.ONE.subtract(rateFactor);
  }
}
