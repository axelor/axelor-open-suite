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
  protected static final int CALCULATION_SCALE = 20;

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

  /**
   * Returns every config that applies to the product. A config can now carry several dimensions
   * (product, product category, product family, stock rotation category) on the same line: it
   * applies only when at least one dimension is set and the product matches <b>all</b> the
   * dimensions set on the line (AND semantics). A line with a dimension the product does not
   * satisfy is skipped. Category matching honors the parent chain, so a config set on a parent
   * category still applies to products in its sub-categories.
   */
  protected List<DepreciationRateConfig> findApplicableConfigs(Product product)
      throws AxelorException {
    List<DepreciationRateConfig> applicableConfigs = new ArrayList<>();
    for (DepreciationRateConfig config : depreciationRateConfigRepository.all().fetch()) {
      if (isApplicable(config, product)) {
        applicableConfigs.add(config);
      }
    }
    return applicableConfigs;
  }

  /**
   * A config applies to a product when it sets at least one dimension and the product matches every
   * dimension set on the line (AND semantics). A line carrying a dimension the product does not
   * satisfy is skipped. Category matching honors the parent chain (see {@link
   * #categoryMatches(ProductCategory, ProductCategory)}).
   */
  protected boolean isApplicable(DepreciationRateConfig config, Product product)
      throws AxelorException {
    boolean hasDimension = false;

    if (config.getProduct() != null) {
      hasDimension = true;
      if (!config.getProduct().equals(product)) {
        return false;
      }
    }
    if (config.getProductCategory() != null) {
      hasDimension = true;
      if (!categoryMatches(config.getProductCategory(), product.getProductCategory())) {
        return false;
      }
    }
    if (config.getProductFamily() != null) {
      hasDimension = true;
      if (!config.getProductFamily().equals(product.getProductFamily())) {
        return false;
      }
    }
    if (config.getStockRotationCategory() != null) {
      hasDimension = true;
      if (!config.getStockRotationCategory().equals(product.getStockRotationCategory())) {
        return false;
      }
    }

    return hasDimension;
  }

  /**
   * A product matches a config category when its own category is that category or one of its
   * descendants (i.e. the config category belongs to the product category parent chain), so a
   * config set on a parent category still applies to products in its sub-categories. Uses {@link
   * ProductCategoryService#fetchParentCategoryList} for cycle-safe traversal.
   */
  protected boolean categoryMatches(ProductCategory configCategory, ProductCategory productCategory)
      throws AxelorException {
    if (productCategory == null) {
      return false;
    }
    if (configCategory.equals(productCategory)) {
      return true;
    }
    return productCategoryService.fetchParentCategoryList(productCategory).contains(configCategory);
  }

  protected BigDecimal signedRateFactor(DepreciationRateConfig config) {
    BigDecimal rate = config.getDefaultRate() != null ? config.getDefaultRate() : BigDecimal.ZERO;
    BigDecimal rateFactor = rate.divide(HUNDRED, CALCULATION_SCALE, RoundingMode.HALF_UP);
    return config.getTypeSelect() == DepreciationRateConfigRepository.TYPE_VALORIZATION
        ? BigDecimal.ONE.add(rateFactor)
        : BigDecimal.ONE.subtract(rateFactor);
  }
}
