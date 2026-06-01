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
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.repo.StockLocationLineHistoryRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.service.StockLocationLineHistoryService;
import com.axelor.apps.stock.service.WeightedAveragePriceService;
import com.axelor.apps.stock.translation.ITranslation;
import com.axelor.apps.supplychain.db.UnitCostCalcLine;
import com.axelor.apps.supplychain.db.UnitCostCalculation;
import com.axelor.apps.supplychain.db.repo.DepreciationRateConfigRepository;
import com.axelor.apps.supplychain.db.repo.UnitCostCalculationRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.DepRateAggregationService.AggregatedRates;
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

  protected static final BigDecimal HUNDRED = new BigDecimal("100");
  protected static final int CALCULATION_SCALE = 20;

  protected final ProductRepository productRepository;
  protected final UnitCostCalculationRepository unitCostCalculationRepository;
  protected final AppBaseService appBaseService;
  protected final ProductCompanyService productCompanyService;
  protected final DepRateAggregationService depRateAggregationService;
  protected final DepRateCalculationProductService depRateCalculationProductService;
  protected final StockLocationLineRepository stockLocationLineRepository;
  protected final StockLocationLineHistoryService stockLocationLineHistoryService;
  protected final WeightedAveragePriceService weightedAveragePriceService;

  @Inject
  public DepRateCalculationServiceImpl(
      ProductRepository productRepository,
      UnitCostCalculationRepository unitCostCalculationRepository,
      AppBaseService appBaseService,
      ProductCompanyService productCompanyService,
      DepRateAggregationService depRateAggregationService,
      DepRateCalculationProductService depRateCalculationProductService,
      StockLocationLineRepository stockLocationLineRepository,
      StockLocationLineHistoryService stockLocationLineHistoryService,
      WeightedAveragePriceService weightedAveragePriceService) {
    this.productRepository = productRepository;
    this.unitCostCalculationRepository = unitCostCalculationRepository;
    this.appBaseService = appBaseService;
    this.productCompanyService = productCompanyService;
    this.depRateAggregationService = depRateAggregationService;
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
  protected void rateCalculation(UnitCostCalculation unitCostCalculation, Product product)
      throws AxelorException {

    AggregatedRates aggregated = depRateAggregationService.aggregate(product);
    UnitCostCalcLine unitCostCalcLine = createUnitCostCalcLine(product, aggregated);

    unitCostCalculation.addUnitCostCalcLineListItem(unitCostCalcLine);
    unitCostCalculationRepository.save(unitCostCalculation);
  }

  protected UnitCostCalcLine createUnitCostCalcLine(Product product, AggregatedRates rates) {
    UnitCostCalcLine unitCostCalcLine = new UnitCostCalcLine();
    unitCostCalcLine.setProduct(product);
    unitCostCalcLine.setStockRotationCategory(product.getStockRotationCategory());
    BigDecimal previousCost = getCurrentProductAvgPrice(product);
    BigDecimal qty = computeProductQuantity(product);
    BigDecimal computedCost = computeNewCost(previousCost, rates.costToApply(), rates.typeSelect());
    unitCostCalcLine.setTypeSelect(rates.typeSelect());
    unitCostCalcLine.setPreviousCost(previousCost);
    unitCostCalcLine.setPreviousRate(
        product.getRevaluationRate() != null ? product.getRevaluationRate() : BigDecimal.ZERO);
    unitCostCalcLine.setComputedCost(computedCost);
    unitCostCalcLine.setMinRate(rates.minRate());
    unitCostCalcLine.setMaxRate(rates.maxRate());
    unitCostCalcLine.setCostToApply(rates.costToApply());
    unitCostCalcLine.setQty(qty);
    unitCostCalcLine.setTotalPrice(
        qty.multiply(previousCost)
            .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP));
    unitCostCalcLine.setValuedGap(computeValuedGap(qty, previousCost, computedCost));
    return unitCostCalcLine;
  }

  @Override
  @Transactional
  public void recomputeLineBalances(UnitCostCalculation unitCostCalculation) {
    if (unitCostCalculation == null || unitCostCalculation.getId() == null) {
      return;
    }
    unitCostCalculation = unitCostCalculationRepository.find(unitCostCalculation.getId());
    if (unitCostCalculation.getUnitCostCalcLineList() == null) {
      return;
    }
    for (UnitCostCalcLine line : unitCostCalculation.getUnitCostCalcLineList()) {
      computeLineBalances(line);
    }
    unitCostCalculationRepository.save(unitCostCalculation);
  }

  @Override
  public UnitCostCalcLine computeLineBalances(UnitCostCalcLine unitCostCalcLine) {
    BigDecimal previousCost =
        unitCostCalcLine.getPreviousCost() != null
            ? unitCostCalcLine.getPreviousCost()
            : BigDecimal.ZERO;
    BigDecimal qty =
        unitCostCalcLine.getQty() != null ? unitCostCalcLine.getQty() : BigDecimal.ZERO;

    // A line with no type is ignored on update: clear every rate-derived value.
    if (!hasType(unitCostCalcLine)) {
      unitCostCalcLine.setTypeSelect(null);
      unitCostCalcLine.setCostToApply(null);
      unitCostCalcLine.setComputedCost(null);
      unitCostCalcLine.setValuedGap(null);
      return unitCostCalcLine;
    }

    BigDecimal computedCost =
        computeNewCost(
            previousCost, unitCostCalcLine.getCostToApply(), unitCostCalcLine.getTypeSelect());
    unitCostCalcLine.setComputedCost(computedCost);
    unitCostCalcLine.setValuedGap(computeValuedGap(qty, previousCost, computedCost));
    return unitCostCalcLine;
  }

  /** A line is revalued only if it carries a depreciation or valorization type (not null nor 0). */
  protected boolean hasType(UnitCostCalcLine unitCostCalcLine) {
    Integer typeSelect = unitCostCalcLine.getTypeSelect();
    return typeSelect != null
        && (typeSelect == DepreciationRateConfigRepository.TYPE_DEPRECIATION
            || typeSelect == DepreciationRateConfigRepository.TYPE_VALORIZATION);
  }

  /**
   * Compute the new unit cost that will replace the current WAP after applying the rate. For
   * depreciation: previousCost x (1 - rate/100). For valorization: previousCost x (1 + rate/100).
   */
  protected BigDecimal computeNewCost(BigDecimal previousCost, BigDecimal rate, int typeSelect) {
    if (previousCost == null || rate == null || previousCost.signum() == 0) {
      return previousCost != null ? previousCost : BigDecimal.ZERO;
    }
    BigDecimal rateFactor = rate.divide(HUNDRED, CALCULATION_SCALE, RoundingMode.HALF_UP);
    BigDecimal factor =
        typeSelect == DepreciationRateConfigRepository.TYPE_VALORIZATION
            ? BigDecimal.ONE.add(rateFactor)
            : BigDecimal.ONE.subtract(rateFactor);
    return previousCost
        .multiply(factor)
        .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
  }

  /**
   * Read the current WAP of the product using {@link ProductCompanyService} so the company-specific
   * value is returned when avgPrice is configured as a company-specific field. Falls back to the
   * global {@code product.avgPrice} otherwise.
   */
  protected BigDecimal getCurrentProductAvgPrice(Product product) {
    if (product == null) {
      return BigDecimal.ZERO;
    }
    com.axelor.apps.base.db.Company company =
        Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);
    try {
      Object value = productCompanyService.get(product, "avgPrice", company);
      if (value instanceof BigDecimal) {
        return (BigDecimal) value;
      }
    } catch (Exception e) {
      // Fallback below if the company-specific lookup fails (e.g. company is null
      // and the field is configured as company-specific).
    }
    return product.getAvgPrice() != null ? product.getAvgPrice() : BigDecimal.ZERO;
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
    return newTotal
        .subtract(previousTotal)
        .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
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

      // Lines whose type was cleared after calculation are skipped: their product is left
      // untouched.
      if (!hasType(unitCostCalcLine)) {
        continue;
      }

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
      // Lines with no type are ignored on update, so they are not validated.
      if (!hasType(unitCostCalcLine)) {
        continue;
      }
      BigDecimal costToApply = unitCostCalcLine.getCostToApply();
      if (costToApply == null) {
        return false;
      }
      BigDecimal minRate = unitCostCalcLine.getMinRate();
      BigDecimal maxRate = unitCostCalcLine.getMaxRate();
      if (minRate != null && costToApply.compareTo(minRate) < 0) {
        return false;
      }
      if (maxRate != null && costToApply.compareTo(maxRate) > 0) {
        return false;
      }
    }
    return true;
  }

  @Transactional(rollbackOn = Exception.class)
  protected void updateDepreciationRate(UnitCostCalcLine unitCostCalcLine) throws AxelorException {
    // Re-fetch the product within this transaction to avoid LazyInitializationException
    // on the Hibernate proxy carried by unitCostCalcLine from the outer scope.
    Product product = productRepository.find(unitCostCalcLine.getProduct().getId());
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

    BigDecimal rateFactor = rate.divide(HUNDRED, CALCULATION_SCALE, RoundingMode.HALF_UP);
    boolean isValorization = typeSelect == DepreciationRateConfigRepository.TYPE_VALORIZATION;
    BigDecimal factor =
        isValorization ? BigDecimal.ONE.add(rateFactor) : BigDecimal.ONE.subtract(rateFactor);
    LocalDateTime dateT = getCurrentDateTime();

    String originLabel =
        isValorization ? ITranslation.STOCK_VALORIZATION : ITranslation.STOCK_DEPRECIATION;
    String origin =
        String.format(
            "%s (%s%s%%)",
            I18n.get(originLabel),
            isValorization ? "+" : "-",
            rate.stripTrailingZeros().toPlainString());

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
          currentAvgPrice
              .multiply(factor)
              .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
      stockLocationLine.setAvgPrice(newAvgPrice);
      stockLocationLineRepository.save(stockLocationLine);

      stockLocationLineHistoryService.saveHistory(
          stockLocationLine,
          dateT,
          origin,
          StockLocationLineHistoryRepository.TYPE_SELECT_STOCK_REVALUATION);
    }

    weightedAveragePriceService.computeAvgPriceForProduct(product);
  }
}
