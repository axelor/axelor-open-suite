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
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.stock.service.WeightedAveragePriceService;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductMergeStockServiceImpl implements ProductMergeStockService {

  protected final StockLocationLineRepository stockLocationLineRepository;
  protected final StockLocationLineService stockLocationLineService;
  protected final TrackingNumberRepository trackingNumberRepository;
  protected final WeightedAveragePriceService weightedAveragePriceService;
  protected final ReservedQtyService reservedQtyService;
  protected final AppBaseService appBaseService;

  @Inject
  public ProductMergeStockServiceImpl(
      StockLocationLineRepository stockLocationLineRepository,
      StockLocationLineService stockLocationLineService,
      TrackingNumberRepository trackingNumberRepository,
      WeightedAveragePriceService weightedAveragePriceService,
      ReservedQtyService reservedQtyService,
      AppBaseService appBaseService) {
    this.stockLocationLineRepository = stockLocationLineRepository;
    this.stockLocationLineService = stockLocationLineService;
    this.trackingNumberRepository = trackingNumberRepository;
    this.weightedAveragePriceService = weightedAveragePriceService;
    this.reservedQtyService = reservedQtyService;
    this.appBaseService = appBaseService;
  }

  @Override
  public List<String> getStockBlockingChecks(Product absorbedProduct, Product keptProduct) {
    List<String> checks = new ArrayList<>();
    if (!Boolean.TRUE.equals(keptProduct.getStockManaged())
        && stockLocationLineRepository
                .all()
                .filter("self.product = :product")
                .bind("product", absorbedProduct)
                .count()
            > 0) {
      checks.add(
          I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_KEPT_PRODUCT_NOT_STOCK_MANAGED));
    }
    return checks;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void transferStock(Product absorbedProduct, Product keptProduct, ProductMergeResult result)
      throws AxelorException {
    // the tracking numbers are moved first: the detail lines of a stock location are identified by
    // their tracking number
    transferTrackingNumbers(absorbedProduct, keptProduct, result);
    transferStockLocationLines(absorbedProduct, keptProduct, result);
    recomputeReservedQuantities(absorbedProduct, keptProduct);
    weightedAveragePriceService.computeAvgPriceForProduct(keptProduct);
  }

  /**
   * Moves the tracking numbers of the absorbed product to the kept product. A tracking number
   * sequence is unique for a product, so a sequence the kept product already has is renamed and the
   * renaming is written in the merge log.
   */
  protected void transferTrackingNumbers(
      Product absorbedProduct, Product keptProduct, ProductMergeResult result) {
    List<TrackingNumber> absorbedTrackingNumberList =
        trackingNumberRepository
            .all()
            .filter("self.product = :product")
            .bind("product", absorbedProduct)
            .order("trackingNumberSeq")
            .fetch();
    if (absorbedTrackingNumberList.isEmpty()) {
      return;
    }

    Set<String> keptSeqSet = getTrackingNumberSeqSet(keptProduct);
    // the sequences of the absorbed product are taken too: renaming one of them must not take the
    // sequence of another one which has not been moved yet
    Set<String> takenSeqSet = new HashSet<>(keptSeqSet);
    takenSeqSet.addAll(getTrackingNumberSeqSet(absorbedProduct));

    for (TrackingNumber trackingNumber : absorbedTrackingNumberList) {
      String trackingNumberSeq = trackingNumber.getTrackingNumberSeq();
      if (keptSeqSet.contains(trackingNumberSeq)) {
        String newTrackingNumberSeq = getNextFreeTrackingNumberSeq(trackingNumberSeq, takenSeqSet);
        takenSeqSet.add(newTrackingNumberSeq);
        result.addWarning(
            String.format(
                I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_LOG_TRACKING_NUMBER_RENAMED),
                trackingNumberSeq,
                newTrackingNumberSeq));
        trackingNumber.setTrackingNumberSeq(newTrackingNumberSeq);
      }
      trackingNumber.setProduct(keptProduct);
    }
    result.addTransferredReferences(
        TrackingNumber.class.getSimpleName() + ".product", absorbedTrackingNumberList.size());
  }

  protected Set<String> getTrackingNumberSeqSet(Product product) {
    return trackingNumberRepository
        .all()
        .filter("self.product = :product")
        .bind("product", product)
        .fetch()
        .stream()
        .map(TrackingNumber::getTrackingNumberSeq)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  /**
   * Returns the sequence suffixed with "-2", as asked by the specification, or with the next free
   * number when that sequence is taken too.
   */
  protected String getNextFreeTrackingNumberSeq(String trackingNumberSeq, Set<String> takenSeqSet) {
    int suffix = 2;
    String newTrackingNumberSeq = trackingNumberSeq + "-" + suffix;
    while (takenSeqSet.contains(newTrackingNumberSeq)) {
      suffix++;
      newTrackingNumberSeq = trackingNumberSeq + "-" + suffix;
    }
    return newTrackingNumberSeq;
  }

  /**
   * Adds the quantities of the absorbed product on the kept product for every stock location, then
   * sets them to zero on the absorbed product.
   */
  protected void transferStockLocationLines(
      Product absorbedProduct, Product keptProduct, ProductMergeResult result)
      throws AxelorException {
    transferDetailStockLocationLines(absorbedProduct, keptProduct, result);

    List<StockLocationLine> absorbedStockLocationLineList =
        stockLocationLineRepository
            .all()
            .filter("self.product = :product AND self.stockLocation IS NOT NULL")
            .bind("product", absorbedProduct)
            .fetch();
    for (StockLocationLine absorbedLine : absorbedStockLocationLineList) {
      mergeStockLocationLine(absorbedLine, keptProduct, result);
    }
  }

  /**
   * Moves the stock detail lines of the absorbed product. A detail line is identified by its
   * tracking number, and a tracking number belongs to a single product, so a detail line of the
   * absorbed product can never meet a detail line of the kept product. A detail line without a
   * tracking number cannot be identified, so it stays on the absorbed product and is reported.
   */
  protected void transferDetailStockLocationLines(
      Product absorbedProduct, Product keptProduct, ProductMergeResult result) {
    int detailLineCount =
        JPA.em()
            .createQuery(
                "UPDATE com.axelor.apps.stock.db.StockLocationLine self "
                    + "SET self.product = :keptProduct "
                    + "WHERE self.product = :absorbedProduct "
                    + "AND self.detailsStockLocation IS NOT NULL "
                    + "AND self.trackingNumber IS NOT NULL")
            .setParameter("keptProduct", keptProduct)
            .setParameter("absorbedProduct", absorbedProduct)
            .executeUpdate();
    result.addTransferredReferences("StockLocationLine.detailsStockLocation", detailLineCount);

    long untrackedDetailLineCount =
        stockLocationLineRepository
            .all()
            .filter(
                "self.product = :product "
                    + "AND self.detailsStockLocation IS NOT NULL "
                    + "AND self.trackingNumber IS NULL")
            .bind("product", absorbedProduct)
            .count();
    if (untrackedDetailLineCount > 0) {
      result.addWarning(
          String.format(
              I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_LOG_DETAIL_LINE_NOT_TRANSFERRED),
              untrackedDetailLineCount));
    }
  }

  protected void mergeStockLocationLine(
      StockLocationLine absorbedLine, Product keptProduct, ProductMergeResult result)
      throws AxelorException {
    StockLocationLine keptLine =
        stockLocationLineService.getOrCreateStockLocationLine(
            absorbedLine.getStockLocation(), keptProduct);
    if (keptLine == null) {
      // the product to keep is not managed in stock, which is refused before the merge starts
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_KEPT_PRODUCT_NOT_STOCK_MANAGED));
    }

    checkSameUnit(absorbedLine, keptLine, keptProduct);

    BigDecimal previousAvgPrice = getValue(keptLine.getAvgPrice());
    BigDecimal newAvgPrice = computeMergedAvgPrice(keptLine, absorbedLine);

    keptLine.setAvgPrice(newAvgPrice);
    keptLine.setCurrentQty(
        getValue(keptLine.getCurrentQty()).add(getValue(absorbedLine.getCurrentQty())));
    keptLine.setFutureQty(
        getValue(keptLine.getFutureQty()).add(getValue(absorbedLine.getFutureQty())));
    keptLine.setLastFutureStockMoveDate(
        getLastDate(
            keptLine.getLastFutureStockMoveDate(), absorbedLine.getLastFutureStockMoveDate()));
    if (keptLine.getUnit() == null) {
      keptLine.setUnit(absorbedLine.getUnit());
    }
    if (StringUtils.isEmpty(keptLine.getRack())) {
      keptLine.setRack(absorbedLine.getRack());
    }

    result.addInformation(
        String.format(
            I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_LOG_STOCK_TRANSFERRED),
            absorbedLine.getStockLocation().getName(),
            format(getValue(absorbedLine.getCurrentQty())),
            format(previousAvgPrice),
            format(newAvgPrice)));

    absorbedLine.setCurrentQty(BigDecimal.ZERO);
    absorbedLine.setFutureQty(BigDecimal.ZERO);
    absorbedLine.setReservedQty(BigDecimal.ZERO);
    absorbedLine.setRequestedReservedQty(BigDecimal.ZERO);

    // a line built by getOrCreateStockLocationLine is not persisted yet
    stockLocationLineRepository.save(keptLine);
  }

  /**
   * The two products have the same unit, it is checked before the merge, but a stock location line
   * carries its own unit: adding quantities expressed in two different units is refused. A line
   * without a unit is read with the unit of the product it belongs to.
   */
  protected void checkSameUnit(
      StockLocationLine absorbedLine, StockLocationLine keptLine, Product keptProduct)
      throws AxelorException {
    Unit absorbedUnit = getUnit(absorbedLine, absorbedLine.getProduct());
    Unit keptUnit = getUnit(keptLine, keptProduct);
    if (absorbedUnit != null && keptUnit != null && !absorbedUnit.equals(keptUnit)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.PRODUCT_MERGE_DIFFERENT_STOCK_LOCATION_LINE_UNIT),
          absorbedLine.getStockLocation().getName());
    }
  }

  protected Unit getUnit(StockLocationLine stockLocationLine, Product product) {
    return stockLocationLine.getUnit() != null
        ? stockLocationLine.getUnit()
        : Optional.ofNullable(product).map(Product::getUnit).orElse(null);
  }

  /**
   * Returns the weighted average price of the two lines, following the same computation as the one
   * applied when stock is received (see {@code
   * StockMoveLineServiceImpl#computeNewAveragePriceLocationLine}): when the two quantities cancel
   * each other, the price of the kept product is left as it is.
   */
  protected BigDecimal computeMergedAvgPrice(
      StockLocationLine keptLine, StockLocationLine absorbedLine) {
    BigDecimal keptQty = getValue(keptLine.getCurrentQty());
    BigDecimal absorbedQty = getValue(absorbedLine.getCurrentQty());
    BigDecimal keptAvgPrice = getValue(keptLine.getAvgPrice());

    BigDecimal denominator = keptQty.add(absorbedQty);
    if (denominator.compareTo(BigDecimal.ZERO) == 0) {
      return keptAvgPrice;
    }

    BigDecimal sum =
        keptAvgPrice
            .multiply(keptQty)
            .add(getValue(absorbedLine.getAvgPrice()).multiply(absorbedQty));
    return sum.divide(
        denominator, appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
  }

  /**
   * Computes the reserved quantities of every stock location line of the two products again. They
   * are read from the planned stock moves, which are already on the kept product: adding the two
   * quantities would count the reservations of the absorbed product twice.
   */
  protected void recomputeReservedQuantities(Product absorbedProduct, Product keptProduct)
      throws AxelorException {
    for (Product product : Arrays.asList(absorbedProduct, keptProduct)) {
      List<StockLocationLine> stockLocationLineList =
          stockLocationLineRepository
              .all()
              .filter("self.product = :product AND self.stockLocation IS NOT NULL")
              .bind("product", product)
              .fetch();
      for (StockLocationLine stockLocationLine : stockLocationLineList) {
        reservedQtyService.updateRequestedReservedQty(stockLocationLine);
        reservedQtyService.updateReservedQty(stockLocationLine);
      }
    }
  }

  /** Quantities and prices are stored with ten decimals: only the meaningful ones are logged. */
  protected String format(BigDecimal value) {
    return getValue(value).stripTrailingZeros().toPlainString();
  }

  protected BigDecimal getValue(BigDecimal value) {
    return Optional.ofNullable(value).orElse(BigDecimal.ZERO);
  }

  protected LocalDate getLastDate(LocalDate keptDate, LocalDate absorbedDate) {
    if (keptDate == null) {
      return absorbedDate;
    }
    if (absorbedDate == null) {
      return keptDate;
    }
    return keptDate.isAfter(absorbedDate) ? keptDate : absorbedDate;
  }
}
