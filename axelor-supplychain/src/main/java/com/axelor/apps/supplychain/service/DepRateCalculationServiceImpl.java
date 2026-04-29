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
import com.axelor.apps.base.db.ProductFamily;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.repo.StockLocationLineHistoryRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.service.StockLocationLineHistoryService;
import com.axelor.apps.stock.service.WeightedAveragePriceService;
import com.axelor.apps.supplychain.db.DepreciationRateConfig;
import com.axelor.apps.supplychain.db.UnitCostCalcLine;
import com.axelor.apps.supplychain.db.UnitCostCalculation;
import com.axelor.apps.supplychain.db.repo.DepreciationRateConfigRepository;
import com.axelor.apps.supplychain.db.repo.UnitCostCalculationRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class DepRateCalculationServiceImpl implements DepRateCalculationService {

  protected static final int WAP_SCALE = 10;
  protected static final BigDecimal HUNDRED = new BigDecimal("100");

  protected final ProductRepository productRepository;
  protected final UnitCostCalculationRepository unitCostCalculationRepository;
  protected final AppBaseService appBaseService;
  protected final DepreciationRateConfigRepository depreciationRateConfigRepository;
  protected final DepRateCalculationProductService depRateCalculationProductService;
  protected final StockLocationLineRepository stockLocationLineRepository;
  protected final StockLocationLineHistoryService stockLocationLineHistoryService;
  protected final WeightedAveragePriceService weightedAveragePriceService;

  @Inject
  public DepRateCalculationServiceImpl(
      ProductRepository productRepository,
      UnitCostCalculationRepository unitCostCalculationRepository,
      AppBaseService appBaseService,
      DepreciationRateConfigRepository depreciationRateConfigRepository,
      DepRateCalculationProductService depRateCalculationProductService,
      StockLocationLineRepository stockLocationLineRepository,
      StockLocationLineHistoryService stockLocationLineHistoryService,
      WeightedAveragePriceService weightedAveragePriceService) {
    this.productRepository = productRepository;
    this.unitCostCalculationRepository = unitCostCalculationRepository;
    this.appBaseService = appBaseService;
    this.depreciationRateConfigRepository = depreciationRateConfigRepository;
    this.depRateCalculationProductService = depRateCalculationProductService;
    this.stockLocationLineRepository = stockLocationLineRepository;
    this.stockLocationLineHistoryService = stockLocationLineHistoryService;
    this.weightedAveragePriceService = weightedAveragePriceService;
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

  @Transactional
  protected void clear(UnitCostCalculation unitCostCalculation) {
    unitCostCalculation.clearUnitCostCalcLineList();
    unitCostCalculationRepository.save(unitCostCalculation);
  }

  @Transactional
  protected void updateStatusToComputed(UnitCostCalculation unitCostCalculation) {
    unitCostCalculation.setCalculationDateTime(getCurrentDateTime());
    unitCostCalculation.setStatusSelect(UnitCostCalculationRepository.STATUS_COSTS_COMPUTED);
    unitCostCalculationRepository.save(unitCostCalculation);
  }

  @Transactional
  protected void updateStatusProductCostPriceUpdated(UnitCostCalculation unitCostCalculation) {
    unitCostCalculation.setUpdateCostDateTime(getCurrentDateTime());
    unitCostCalculation.setStatusSelect(UnitCostCalculationRepository.STATUS_COSTS_UPDATED);
    unitCostCalculationRepository.save(unitCostCalculation);
  }

  protected LocalDateTime getCurrentDateTime() {
    return appBaseService
        .getTodayDateTime(
            Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null))
        .toLocalDateTime();
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

    DepreciationRateConfig depreciationRateConfig = resolveDepreciationRateConfig(product);

    BigDecimal minRate = BigDecimal.ZERO;
    BigDecimal maxRate = BigDecimal.ZERO;
    BigDecimal defaultRate = BigDecimal.ZERO;
    int typeSelect = DepreciationRateConfigRepository.TYPE_DEPRECIATION;
    if (depreciationRateConfig != null) {
      minRate =
          depreciationRateConfig.getMinRate() != null
              ? depreciationRateConfig.getMinRate()
              : BigDecimal.ZERO;
      maxRate =
          depreciationRateConfig.getMaxRate() != null
              ? depreciationRateConfig.getMaxRate()
              : BigDecimal.ZERO;
      defaultRate =
          depreciationRateConfig.getDefaultRate() != null
              ? depreciationRateConfig.getDefaultRate()
              : BigDecimal.ZERO;
      typeSelect = depreciationRateConfig.getTypeSelect();
    }

    UnitCostCalcLine unitCostCalcLine =
        createUnitCostCalcLine(product, defaultRate, minRate, maxRate, typeSelect);

    unitCostCalculation.addUnitCostCalcLineListItem(unitCostCalcLine);
    unitCostCalculationRepository.save(unitCostCalculation);
  }

  /**
   * Resolve the applicable depreciation rate config by priority: product &gt; productCategory (with
   * parents) &gt; productFamily &gt; stockRotationCategory. Returns the first non-null match.
   */
  protected DepreciationRateConfig resolveDepreciationRateConfig(Product product) {
    DepreciationRateConfig depreciationRateConfig =
        depreciationRateConfigRepository
            .all()
            .filter("self.product = :product")
            .bind("product", product)
            .fetchOne();
    if (depreciationRateConfig != null) {
      return depreciationRateConfig;
    }

    if (product.getProductCategory() != null) {
      depreciationRateConfig = findDepRateConfigFromCategory(product.getProductCategory(), null);
      if (depreciationRateConfig != null) {
        return depreciationRateConfig;
      }
    }

    if (product.getProductFamily() != null) {
      depreciationRateConfig = findDepRateConfigFromFamily(product.getProductFamily());
      if (depreciationRateConfig != null) {
        return depreciationRateConfig;
      }
    }

    if (product.getStockRotationCategory() != null) {
      depreciationRateConfig =
          depreciationRateConfigRepository
              .all()
              .filter("self.stockRotationCategory = :stockRotationCategory")
              .bind("stockRotationCategory", product.getStockRotationCategory())
              .fetchOne();
    }

    return depreciationRateConfig;
  }

  protected UnitCostCalcLine createUnitCostCalcLine(
      Product product,
      BigDecimal defaultRate,
      BigDecimal minRate,
      BigDecimal maxRate,
      int typeSelect) {
    UnitCostCalcLine unitCostCalcLine = new UnitCostCalcLine();
    unitCostCalcLine.setProduct(product);
    BigDecimal previousCost =
        product.getAvgPrice() != null ? product.getAvgPrice() : BigDecimal.ZERO;
    BigDecimal qty = computeProductQuantity(product);
    BigDecimal computedCost = computeNewCost(previousCost, defaultRate, typeSelect);
    unitCostCalcLine.setTypeSelect(typeSelect);
    unitCostCalcLine.setPreviousCost(previousCost);
    unitCostCalcLine.setComputedCost(computedCost);
    unitCostCalcLine.setMinRate(minRate);
    unitCostCalcLine.setMaxRate(maxRate);
    unitCostCalcLine.setCostToApply(defaultRate);
    unitCostCalcLine.setQty(qty);
    unitCostCalcLine.setTotalPrice(
        qty.multiply(previousCost).setScale(WAP_SCALE, RoundingMode.HALF_UP));
    unitCostCalcLine.setValuedGap(computeValuedGap(qty, previousCost, computedCost));
    return unitCostCalcLine;
  }

  /**
   * Compute the new unit cost that will replace the current WAP after applying the rate. For
   * depreciation: previousCost x (1 - rate/100). For valorization: previousCost x (1 + rate/100).
   */
  protected BigDecimal computeNewCost(BigDecimal previousCost, BigDecimal rate, int typeSelect) {
    if (previousCost == null || rate == null || previousCost.signum() == 0) {
      return previousCost != null ? previousCost : BigDecimal.ZERO;
    }
    BigDecimal rateFactor = rate.divide(HUNDRED, WAP_SCALE, RoundingMode.HALF_UP);
    BigDecimal factor =
        typeSelect == DepreciationRateConfigRepository.TYPE_VALORIZATION
            ? BigDecimal.ONE.add(rateFactor)
            : BigDecimal.ONE.subtract(rateFactor);
    return previousCost.multiply(factor).setScale(WAP_SCALE, RoundingMode.HALF_UP);
  }

  /** Sum the current quantity across all non-virtual stock location lines for the product. */
  protected BigDecimal computeProductQuantity(Product product) {
    List<StockLocationLine> stockLocationLines =
        stockLocationLineRepository
            .all()
            .filter("self.product = :product AND self.stockLocation.typeSelect != :virtualType")
            .bind("product", product)
            .bind("virtualType", StockLocationRepository.TYPE_VIRTUAL)
            .fetch();

    BigDecimal qty = BigDecimal.ZERO;
    for (StockLocationLine line : stockLocationLines) {
      if (line.getCurrentQty() != null) {
        qty = qty.add(line.getCurrentQty());
      }
    }
    return qty;
  }

  /**
   * Compute qty x (computedCost - previousCost): the valuation change after applying the rate.
   * Negative for depreciation, positive for valorization.
   */
  protected BigDecimal computeValuedGap(
      BigDecimal qty, BigDecimal previousCost, BigDecimal computedCost) {
    if (qty == null || previousCost == null || computedCost == null || qty.signum() == 0) {
      return BigDecimal.ZERO;
    }
    BigDecimal previousTotal = qty.multiply(previousCost);
    BigDecimal newTotal = qty.multiply(computedCost);
    return newTotal.subtract(previousTotal).setScale(WAP_SCALE, RoundingMode.HALF_UP);
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

  protected DepreciationRateConfig findDepRateConfigFromFamily(ProductFamily productFamily) {
    return depreciationRateConfigRepository
        .all()
        .filter("self.productFamily = :productFamily")
        .bind("productFamily", productFamily)
        .fetchOne();
  }

  @Override
  public void updateDepRates(UnitCostCalculation unitCostCalculation) throws AxelorException {

    if (!ratesOk(unitCostCalculation)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(SupplychainExceptionMessage.DEPRECIATION_CALCULATION_INVALID_RATES));
    }

    int i = 0;
    for (UnitCostCalcLine unitCostCalcLine : unitCostCalculation.getUnitCostCalcLineList()) {

      updateDepreciationRate(unitCostCalcLine);

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
  protected void updateDepreciationRate(UnitCostCalcLine unitCostCalcLine) throws AxelorException {
    Product product = unitCostCalcLine.getProduct();
    BigDecimal rate = unitCostCalcLine.getCostToApply();
    int typeSelect = unitCostCalcLine.getTypeSelect();

    // Store as signed rate: negative for depreciation, positive for valorization.
    BigDecimal signedRate = rate;
    if (rate != null && typeSelect == DepreciationRateConfigRepository.TYPE_DEPRECIATION) {
      signedRate = rate.negate();
    }
    product.setRevaluationRate(signedRate);
    productRepository.save(product);

    applyRateOnStockLocationLines(product, rate, typeSelect);
  }

  /**
   * Apply the rate to every non-virtual StockLocationLine of the product: update the avgPrice
   * (decreased for depreciation, increased for valorization), create a StockLocationLineHistory
   * entry, then recompute the product's WAP.
   */
  protected void applyRateOnStockLocationLines(Product product, BigDecimal rate, int typeSelect)
      throws AxelorException {
    if (rate == null || rate.signum() == 0) {
      return;
    }

    BigDecimal rateFactor = rate.divide(HUNDRED, WAP_SCALE, RoundingMode.HALF_UP);
    BigDecimal factor =
        typeSelect == DepreciationRateConfigRepository.TYPE_VALORIZATION
            ? BigDecimal.ONE.add(rateFactor)
            : BigDecimal.ONE.subtract(rateFactor);
    LocalDateTime dateT = getCurrentDateTime();

    List<StockLocationLine> stockLocationLines =
        stockLocationLineRepository
            .all()
            .filter("self.product = :product AND self.stockLocation.typeSelect != :virtualType")
            .bind("product", product)
            .bind("virtualType", StockLocationRepository.TYPE_VIRTUAL)
            .fetch();

    for (StockLocationLine stockLocationLine : stockLocationLines) {
      BigDecimal currentAvgPrice = stockLocationLine.getAvgPrice();
      if (currentAvgPrice == null || currentAvgPrice.signum() == 0) {
        continue;
      }
      BigDecimal newAvgPrice =
          currentAvgPrice.multiply(factor).setScale(WAP_SCALE, RoundingMode.HALF_UP);
      stockLocationLine.setAvgPrice(newAvgPrice);
      stockLocationLineRepository.save(stockLocationLine);

      stockLocationLineHistoryService.saveHistory(
          stockLocationLine,
          dateT,
          StockLocationLineHistoryRepository.ORIGIN_INVENTORY_DEPRECIATION,
          StockLocationLineHistoryRepository.TYPE_SELECT_INVENTORY_DEPRECIATION);
    }

    weightedAveragePriceService.computeAvgPriceForProduct(product);
  }
}
