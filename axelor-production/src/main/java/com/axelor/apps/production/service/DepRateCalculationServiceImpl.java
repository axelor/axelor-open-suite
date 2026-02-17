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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.DepreciationRateConfig;
import com.axelor.apps.production.db.UnitCostCalcLine;
import com.axelor.apps.production.db.UnitCostCalculation;
import com.axelor.apps.production.db.repo.DepreciationRateConfigRepository;
import com.axelor.apps.production.db.repo.UnitCostCalcLineRepository;
import com.axelor.apps.production.db.repo.UnitCostCalculationRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.costsheet.CostSheetService;
import com.axelor.apps.production.service.costsheet.UnitCostCalcLineService;
import com.axelor.apps.production.service.costsheet.UnitCostCalculationServiceImpl;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;

public class DepRateCalculationServiceImpl extends UnitCostCalculationServiceImpl
    implements DepRateCalculationService {

  protected final DepreciationRateConfigRepository depreciationRateConfigRepository;
  protected final ProductStockAnalysisService productStockAnalysisService;
  protected final DepRateCalculationProductService depRateCalculationProductService;

  @Inject
  public DepRateCalculationServiceImpl(
      ProductRepository productRepository,
      UnitCostCalculationRepository unitCostCalculationRepository,
      UnitCostCalcLineService unitCostCalcLineService,
      CostSheetService costSheetService,
      UnitCostCalcLineRepository unitCostCalcLineRepository,
      AppProductionService appProductionService,
      ProductService productService,
      ProductCompanyService productCompanyService,
      AppBaseService appBaseService,
      BillOfMaterialService billOfMaterialService,
      DepreciationRateConfigRepository depreciationRateConfigRepository,
      ProductStockAnalysisService productStockAnalysisService,
      DepRateCalculationProductService depRateCalculationProductService) {
    super(
        productRepository,
        unitCostCalculationRepository,
        unitCostCalcLineService,
        costSheetService,
        unitCostCalcLineRepository,
        appProductionService,
        productService,
        productCompanyService,
        appBaseService,
        billOfMaterialService);
    this.depreciationRateConfigRepository = depreciationRateConfigRepository;
    this.productStockAnalysisService = productStockAnalysisService;
    this.depRateCalculationProductService = depRateCalculationProductService;
  }

  @Override
  public void runDepRateCalc(UnitCostCalculation unitCostCalculation) throws AxelorException {

    if (!unitCostCalculation.getUnitCostCalcLineList().isEmpty()) {
      clear(unitCostCalculation);
    }

    unitCostCalculation = unitCostCalculationRepository.find(unitCostCalculation.getId());

    rateCalculation(unitCostCalculation);

    updateStatusToComputed(unitCostCalculationRepository.find(unitCostCalculation.getId()));
  }

  protected void rateCalculation(UnitCostCalculation unitCostCalculation) throws AxelorException {
    int i = 0;
    for (Product product : depRateCalculationProductService.getProducts(unitCostCalculation)) {

      rateCalculation(
          unitCostCalculationRepository.find(unitCostCalculation.getId()),
          productRepository.find(product.getId()));

      if (++i % AbstractBatch.FETCH_LIMIT == 0) {
        JPA.clear();
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void rateCalculation(UnitCostCalculation unitCostCalculation, Product product) {

    DepreciationRateConfig depreciationRateStockCategoryConfig = getDepreciationRateConfig(product);

    // Find config by product or category
    DepreciationRateConfig depreciationRateConfig =
        depreciationRateConfigRepository
            .all()
            .filter("self.product = :product")
            .bind("product", product)
            .fetchOne();

    if (product.getProductCategory() != null) {
      depreciationRateConfig =
          findDepRateConfigFromCategory(product.getProductCategory(), depreciationRateConfig);
    }

    // Determine rates
    BigDecimal minRate =
        computeMinRate(depreciationRateStockCategoryConfig, depreciationRateConfig);

    BigDecimal defaultRate = BigDecimal.ZERO;
    BigDecimal maxRate = BigDecimal.ZERO;
    if (depreciationRateConfig != null) {
      defaultRate = depreciationRateConfig.getDefaultRate();
      maxRate = depreciationRateConfig.getMaxRate();
    }

    UnitCostCalcLine unitCostCalcLine =
        createUnitCostCalcLine(product, defaultRate, minRate, maxRate);

    unitCostCalculation.addUnitCostCalcLineListItem(unitCostCalcLine);
    unitCostCalculationRepository.save(unitCostCalculation);
  }

  protected DepreciationRateConfig getDepreciationRateConfig(Product product) {
    DepreciationRateConfig depreciationRateStockCategoryConfig = null;

    // Find config by stock category (for slow/fast movers with bracket)
    if (product.getStockCategorySelect() == ProductRepository.STOCK_CATEGORY_SLOW_MOVERS) {
      BigDecimal coverageInDays = productStockAnalysisService.computeSlowOrFastMover(product);

      depreciationRateStockCategoryConfig =
          depreciationRateConfigRepository
              .all()
              .filter(
                  "self.stockCategorySelect = :stockCategorySelect AND :coverageInDays <= self.greaterBracket")
              .bind("stockCategorySelect", product.getStockCategorySelect())
              .bind("coverageInDays", coverageInDays)
              .order("greaterBracket")
              .fetchOne();
    } else {
      depreciationRateStockCategoryConfig =
          depreciationRateConfigRepository
              .all()
              .filter("self.stockCategorySelect = :stockCategorySelect")
              .bind("stockCategorySelect", product.getStockCategorySelect())
              .fetchOne();
    }
    return depreciationRateStockCategoryConfig;
  }

  protected static BigDecimal computeMinRate(
      DepreciationRateConfig depreciationRateStockCategoryConfig,
      DepreciationRateConfig depreciationRateConfig) {
    BigDecimal minRate = BigDecimal.ZERO;
    if (depreciationRateStockCategoryConfig != null) {
      minRate = depreciationRateStockCategoryConfig.getMinRate();
    } else if (depreciationRateConfig != null) {
      minRate = depreciationRateConfig.getMinRate();
    }
    return minRate;
  }

  protected UnitCostCalcLine createUnitCostCalcLine(
      Product product, BigDecimal defaultRate, BigDecimal minRate, BigDecimal maxRate) {
    UnitCostCalcLine unitCostCalcLine = new UnitCostCalcLine();
    unitCostCalcLine.setProduct(product);
    unitCostCalcLine.setPreviousCost(product.getDepreciationRate());
    unitCostCalcLine.setComputedCost(defaultRate);
    unitCostCalcLine.setMinRate(minRate);
    unitCostCalcLine.setMaxRate(maxRate);
    unitCostCalcLine.setCostToApply(defaultRate);
    return unitCostCalcLine;
  }

  protected DepreciationRateConfig findDepRateConfigFromCategory(
      ProductCategory productCategory, DepreciationRateConfig depreciationRateConfig) {

    DepreciationRateConfig catDepreciationRateConfig =
        depreciationRateConfigRepository
            .all()
            .filter("self.productCategory = :productCategory")
            .bind("productCategory", productCategory)
            .fetchOne();

    if (depreciationRateConfig == null
        || (catDepreciationRateConfig != null
            && catDepreciationRateConfig.getMaxRate() != null
            && depreciationRateConfig.getMaxRate() != null
            && catDepreciationRateConfig.getMaxRate().compareTo(depreciationRateConfig.getMaxRate())
                > 0)) {
      depreciationRateConfig = catDepreciationRateConfig;
    }

    if (productCategory.getParentProductCategory() != null) {
      depreciationRateConfig =
          findDepRateConfigFromCategory(
              productCategory.getParentProductCategory(), depreciationRateConfig);
    }

    return depreciationRateConfig;
  }

  @Override
  public void updateDepRates(UnitCostCalculation unitCostCalculation) throws AxelorException {

    if (!ratesOk(unitCostCalculation)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.DEPRECIATION_CALCULATION_INVALID_RATES));
    }

    int i = 0;
    for (UnitCostCalcLine unitCostCalcLine : unitCostCalculation.getUnitCostCalcLineList()) {

      updateDepreciationRate(unitCostCalcLineRepository.find(unitCostCalcLine.getId()));

      if (++i % AbstractBatch.FETCH_LIMIT == 0) {
        JPA.clear();
      }
    }

    updateStatusProductCostPriceUpdated(
        unitCostCalculationRepository.find(unitCostCalculation.getId()));
  }

  protected boolean ratesOk(UnitCostCalculation unitCostCalculation) {
    for (UnitCostCalcLine unitCostCalcLine : unitCostCalculation.getUnitCostCalcLineList()) {
      if (unitCostCalcLine.getCostToApply() == null
          || unitCostCalcLine.getMinRate() == null
          || unitCostCalcLine.getMaxRate() == null) {
        return false;
      }

      if (unitCostCalcLine.getCostToApply().compareTo(unitCostCalcLine.getMinRate()) < 0
          || unitCostCalcLine.getCostToApply().compareTo(unitCostCalcLine.getMaxRate()) > 0) {
        return false;
      }
    }
    return true;
  }

  @Transactional(rollbackOn = Exception.class)
  protected void updateDepreciationRate(UnitCostCalcLine unitCostCalcLine) {
    Product product = unitCostCalcLine.getProduct();
    product.setDepreciationRate(unitCostCalcLine.getCostToApply());
    productRepository.save(product);
  }
}
