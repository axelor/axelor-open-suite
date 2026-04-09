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
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.utils.JpaModelHelper;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductionTrackingPreservationServiceImpl
    implements ProductionTrackingPreservationService {

  protected final ProductCompanyService productCompanyService;
  protected final StockMoveLineService stockMoveLineService;
  protected final TrackingNumberRepository trackingNumberRepo;
  protected final UnitConversionService unitConversionService;
  protected final AppBaseService appBaseService;

  @Inject
  public ProductionTrackingPreservationServiceImpl(
      ProductCompanyService productCompanyService,
      StockMoveLineService stockMoveLineService,
      TrackingNumberRepository trackingNumberRepo,
      UnitConversionService unitConversionService,
      AppBaseService appBaseService) {
    this.productCompanyService = productCompanyService;
    this.stockMoveLineService = stockMoveLineService;
    this.trackingNumberRepo = trackingNumberRepo;
    this.unitConversionService = unitConversionService;
    this.appBaseService = appBaseService;
  }

  @Override
  public PreservedTrackingNumbersByProduct getPreservedTrackingNumbersByProduct(
      List<StockMoveLine> stockMoveLines) {
    Map<Long, Deque<PreservedTrackingNumber>> result = new HashMap<>();
    if (stockMoveLines == null) {
      return new PreservedTrackingNumbersByProduct(result);
    }

    for (StockMoveLine line : stockMoveLines) {
      TrackingNumber trackingNumber = line.getTrackingNumber();
      Product product = line.getProduct();
      BigDecimal qty = line.getQty();
      if (trackingNumber == null || product == null || qty == null) {
        continue;
      }
      boolean restoreOrigin =
          Optional.ofNullable(trackingNumber.getOriginStockMoveLine())
              .map(StockMoveLine::getId)
              .filter(originLineId -> Objects.equals(originLineId, line.getId()))
              .isPresent();

      result
          .computeIfAbsent(product.getId(), k -> new ArrayDeque<>())
          .add(new PreservedTrackingNumber(trackingNumber, qty, line.getUnit(), restoreOrigin));
    }

    return new PreservedTrackingNumbersByProduct(result);
  }

  @Override
  public PreservedTrackingNumbersByProduct getPreservedTrackingNumbersByProduct(
      List<StockMoveLine> originalStockMoveLines, List<StockMoveLine> currentStockMoveLines) {
    if (originalStockMoveLines == null) {
      return getPreservedTrackingNumbersByProduct(originalStockMoveLines);
    }
    if (currentStockMoveLines == null) {
      return getPreservedTrackingNumbersByProduct(originalStockMoveLines);
    }

    List<StockMoveLine> removedStockMoveLines =
        filterRemovedStockMoveLines(originalStockMoveLines, currentStockMoveLines);
    return getPreservedTrackingNumbersByProduct(removedStockMoveLines);
  }

  protected List<StockMoveLine> filterRemovedStockMoveLines(
      List<StockMoveLine> originalStockMoveLines, List<StockMoveLine> currentStockMoveLines) {
    Set<Long> currentStockMoveLineIds =
        currentStockMoveLines.stream().map(StockMoveLine::getId).collect(Collectors.toSet());

    return originalStockMoveLines.stream()
        .filter(stockMoveLine -> !currentStockMoveLineIds.contains(stockMoveLine.getId()))
        .map(JpaModelHelper::ensureManaged)
        .toList();
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public StockMoveLine createStockMoveLineWithPreservedTracking(
      ProdProduct prodProduct,
      StockMove stockMove,
      int inOrOutType,
      BigDecimal qty,
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      TrackingNumber trackingNumber,
      boolean restoreOrigin)
      throws AxelorException {
    BigDecimal costPrice =
        prodProduct.getProduct() != null
            ? (BigDecimal)
                productCompanyService.get(
                    prodProduct.getProduct(), "costPrice", stockMove.getCompany())
            : BigDecimal.ZERO;

    trackingNumber = JpaModelHelper.ensureManaged(trackingNumber);
    StockMoveLine stockMoveLine =
        stockMoveLineService.createStockMoveLine(
            prodProduct.getProduct(),
            (String)
                productCompanyService.get(prodProduct.getProduct(), "name", stockMove.getCompany()),
            (String)
                productCompanyService.get(
                    prodProduct.getProduct(), "description", stockMove.getCompany()),
            qty,
            costPrice,
            costPrice,
            prodProduct.getUnit(),
            stockMove,
            inOrOutType,
            false,
            BigDecimal.ZERO,
            fromStockLocation,
            toStockLocation,
            trackingNumber);
    stockMove.addStockMoveLineListItem(stockMoveLine);

    if (restoreOrigin && trackingNumber != null) {
      trackingNumber.setOriginStockMoveLine(stockMoveLine);
      if (stockMove.getManufOrder() != null) {
        trackingNumber.setOriginMoveTypeSelect(
            TrackingNumberRepository.ORIGIN_MOVE_TYPE_MANUFACTURING);
        trackingNumber.setOriginManufOrder(stockMove.getManufOrder());
      }
      trackingNumberRepo.save(trackingNumber);
    }

    return stockMoveLine;
  }

  protected StockMoveLine createStockMoveLineWithoutTrackingAssignment(
      ProdProduct prodProduct,
      StockMove stockMove,
      BigDecimal qty,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {
    BigDecimal costPrice =
        prodProduct.getProduct() != null
            ? (BigDecimal)
                productCompanyService.get(
                    prodProduct.getProduct(), "costPrice", stockMove.getCompany())
            : BigDecimal.ZERO;

    StockMoveLine stockMoveLine =
        stockMoveLineService.createStockMoveLine(
            prodProduct.getProduct(),
            (String)
                productCompanyService.get(prodProduct.getProduct(), "name", stockMove.getCompany()),
            (String)
                productCompanyService.get(
                    prodProduct.getProduct(), "description", stockMove.getCompany()),
            qty,
            costPrice,
            costPrice,
            costPrice,
            BigDecimal.ZERO,
            prodProduct.getUnit(),
            stockMove,
            null,
            fromStockLocation,
            toStockLocation);
    stockMove.addStockMoveLineListItem(stockMoveLine);

    return stockMoveLine;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void createStockMoveLinesWithPreservedTracking(
      ProdProduct prodProduct,
      StockMove stockMove,
      int inOrOutType,
      BigDecimal qty,
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      PreservedTrackingNumbersByProduct preservedTrackingNumbersByProduct)
      throws AxelorException {
    if (qty.signum() <= 0) {
      return;
    }

    Long productId = prodProduct.getProduct() != null ? prodProduct.getProduct().getId() : null;
    Deque<PreservedTrackingNumber> deque =
        preservedTrackingNumbersByProduct != null && productId != null
            ? preservedTrackingNumbersByProduct.getByProductId(productId)
            : null;
    Set<Long> reusedTrackingNumberIds = new HashSet<>();
    BigDecimal remainingQty = qty;

    while (deque != null && !deque.isEmpty() && remainingQty.signum() > 0) {
      PreservedTrackingNumber preserved = deque.pollFirst();
      BigDecimal preservedQtyInTargetUnit =
          convertQty(
              preserved.unit(), prodProduct.getUnit(), preserved.qty(), prodProduct.getProduct());
      if (preservedQtyInTargetUnit.signum() <= 0) {
        continue;
      }

      BigDecimal lineQty = preservedQtyInTargetUnit.min(remainingQty);
      createStockMoveLineWithPreservedTracking(
          prodProduct,
          stockMove,
          inOrOutType,
          lineQty,
          fromStockLocation,
          toStockLocation,
          preserved.trackingNumber(),
          preserved.restoreOrigin());
      if (preserved.trackingNumber() != null && preserved.trackingNumber().getId() != null) {
        reusedTrackingNumberIds.add(preserved.trackingNumber().getId());
      }

      remainingQty = remainingQty.subtract(lineQty);

      if (preservedQtyInTargetUnit.compareTo(lineQty) > 0) {
        BigDecimal remainingPreservedQty =
            preserved
                .qty()
                .subtract(
                    convertQty(
                        prodProduct.getUnit(), preserved.unit(), lineQty, prodProduct.getProduct()))
                .max(BigDecimal.ZERO);

        if (remainingPreservedQty.signum() > 0) {
          deque.addFirst(
              new PreservedTrackingNumber(
                  preserved.trackingNumber(), remainingPreservedQty, preserved.unit(), false));
        }
      }
    }

    if (remainingQty.signum() > 0) {
      if (hasProductAutoSelectTracking(prodProduct, stockMove, inOrOutType, fromStockLocation)) {
        StockMoveLine remainingStockMoveLine =
            createStockMoveLineWithoutTrackingAssignment(
                prodProduct, stockMove, remainingQty, fromStockLocation, toStockLocation);
        stockMoveLineService.assignTrackingNumber(
            remainingStockMoveLine, prodProduct.getProduct(), reusedTrackingNumberIds);
      } else {
        createStockMoveLineWithPreservedTracking(
            prodProduct,
            stockMove,
            inOrOutType,
            remainingQty,
            fromStockLocation,
            toStockLocation,
            null,
            false);
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void drainRemainingPreservedTracking(
      List<ProdProduct> prodProductList,
      StockMove stockMove,
      int inOrOutType,
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      PreservedTrackingNumbersByProduct preservedTrackingNumbersByProduct)
      throws AxelorException {
    if (preservedTrackingNumbersByProduct == null
        || preservedTrackingNumbersByProduct.values() == null
        || prodProductList == null) {
      return;
    }
    for (ProdProduct prodProduct : prodProductList) {
      Long productId = prodProduct.getProduct() != null ? prodProduct.getProduct().getId() : null;
      if (productId == null) {
        continue;
      }
      Deque<PreservedTrackingNumber> deque =
          preservedTrackingNumbersByProduct.getByProductId(productId);
      while (deque != null && !deque.isEmpty()) {
        PreservedTrackingNumber preserved = deque.pollFirst();
        if (preserved.qty() != null && preserved.qty().signum() > 0) {
          createStockMoveLineWithPreservedTracking(
              prodProduct,
              stockMove,
              inOrOutType,
              preserved.qty(),
              fromStockLocation,
              toStockLocation,
              preserved.trackingNumber(),
              preserved.restoreOrigin());
        }
      }
    }
  }

  protected boolean hasProductAutoSelectTracking(
      ProdProduct prodProduct,
      StockMove stockMove,
      int inOrOutType,
      StockLocation fromStockLocation)
      throws AxelorException {
    if (inOrOutType != StockMoveLineService.TYPE_IN_PRODUCTIONS
        || fromStockLocation == null
        || prodProduct.getProduct() == null) {
      return false;
    }

    TrackingNumberConfiguration trackingNumberConfiguration =
        (TrackingNumberConfiguration)
            productCompanyService.get(
                prodProduct.getProduct(), "trackingNumberConfiguration", stockMove.getCompany());

    return trackingNumberConfiguration != null
        && trackingNumberConfiguration.getHasProductAutoSelectTrackingNbr();
  }

  protected BigDecimal convertQty(Unit startUnit, Unit endUnit, BigDecimal qty, Product product)
      throws AxelorException {
    if (qty == null || startUnit == null || endUnit == null || Objects.equals(startUnit, endUnit)) {
      return Optional.ofNullable(qty).orElse(BigDecimal.ZERO);
    }

    return unitConversionService.convert(
        startUnit, endUnit, qty, appBaseService.getNbDecimalDigitForQty(), product);
  }
}
