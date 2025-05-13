/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorAlertException;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ShippingCoefService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.CustomsCodeNomenclature;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.LogisticalFormLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockLocationLineHistory;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.apps.stock.db.repo.StockLocationLineHistoryRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppStock;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class StockMoveLineServiceImpl implements StockMoveLineService {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected AppBaseService appBaseService;
  protected AppStockService appStockService;
  protected StockMoveToolService stockMoveToolService;
  protected StockMoveLineRepository stockMoveLineRepository;
  protected StockLocationLineService stockLocationLineService;
  protected UnitConversionService unitConversionService;
  protected TrackingNumberService trackingNumberService;
  protected WeightedAveragePriceService weightedAveragePriceService;
  protected TrackingNumberRepository trackingNumberRepo;
  protected ProductCompanyService productCompanyService;
  protected ShippingCoefService shippingCoefService;
  protected StockLocationLineHistoryService stockLocationLineHistoryService;
  protected StockLocationLineFetchService stockLocationLineFetchService;
  protected TrackingNumberCreateService trackingNumberCreateService;

  @Inject
  public StockMoveLineServiceImpl(
      TrackingNumberService trackingNumberService,
      AppBaseService appBaseService,
      AppStockService appStockService,
      StockMoveToolService stockMoveToolService,
      StockMoveLineRepository stockMoveLineRepository,
      StockLocationLineService stockLocationLineService,
      UnitConversionService unitConversionService,
      WeightedAveragePriceService weightedAveragePriceService,
      TrackingNumberRepository trackingNumberRepo,
      ProductCompanyService productCompanyService,
      ShippingCoefService shippingCoefService,
      StockLocationLineHistoryService stockLocationLineHistoryService,
      StockLocationLineFetchService stockLocationLineFetchService,
      TrackingNumberCreateService trackingNumberCreateService) {
    this.trackingNumberService = trackingNumberService;
    this.appBaseService = appBaseService;
    this.appStockService = appStockService;
    this.stockMoveToolService = stockMoveToolService;
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.stockLocationLineService = stockLocationLineService;
    this.unitConversionService = unitConversionService;
    this.weightedAveragePriceService = weightedAveragePriceService;
    this.trackingNumberRepo = trackingNumberRepo;
    this.productCompanyService = productCompanyService;
    this.shippingCoefService = shippingCoefService;
    this.stockLocationLineHistoryService = stockLocationLineHistoryService;
    this.stockLocationLineFetchService = stockLocationLineFetchService;
    this.trackingNumberCreateService = trackingNumberCreateService;
  }

  @Override
  public StockMoveLine createStockMoveLine(
      Product product,
      String productName,
      String description,
      BigDecimal quantity,
      BigDecimal unitPrice,
      BigDecimal companyUnitPriceUntaxed,
      Unit unit,
      StockMove stockMove,
      int type,
      boolean taxed,
      BigDecimal taxRate,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {

    if (product != null) {

      StockMoveLine stockMoveLine =
          generateStockMoveLineConvertingUnitPrice(
              product,
              productName,
              description,
              quantity,
              unitPrice,
              companyUnitPriceUntaxed,
              BigDecimal.ZERO,
              unit,
              stockMove,
              taxed,
              taxRate,
              fromStockLocation,
              toStockLocation);
      TrackingNumberConfiguration trackingNumberConfiguration =
          (TrackingNumberConfiguration)
              productCompanyService.get(
                  product,
                  "trackingNumberConfiguration",
                  Optional.ofNullable(stockMoveLine.getStockMove())
                      .map(StockMove::getCompany)
                      .orElse(null));

      return assignOrGenerateTrackingNumber(
          stockMoveLine, stockMove, product, trackingNumberConfiguration, type);
    } else {
      return this.createStockMoveLine(
          product,
          productName,
          description,
          quantity,
          BigDecimal.ZERO,
          BigDecimal.ZERO,
          companyUnitPriceUntaxed,
          BigDecimal.ZERO,
          unit,
          stockMove,
          null,
          fromStockLocation,
          toStockLocation);
    }
  }

  @Override
  public StockMoveLine createStockMoveLine(
      Product product,
      String productName,
      String description,
      BigDecimal quantity,
      BigDecimal unitPrice,
      BigDecimal companyUnitPriceUntaxed,
      Unit unit,
      StockMove stockMove,
      int type,
      boolean taxed,
      BigDecimal taxRate,
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      TrackingNumber trackingNumber)
      throws AxelorException {

    if (product != null) {

      StockMoveLine stockMoveLine =
          generateStockMoveLineConvertingUnitPrice(
              product,
              productName,
              description,
              quantity,
              unitPrice,
              companyUnitPriceUntaxed,
              BigDecimal.ZERO,
              unit,
              stockMove,
              taxed,
              taxRate,
              fromStockLocation,
              toStockLocation);

      if (trackingNumber != null) {
        stockMoveLine.setTrackingNumber(trackingNumber);
        return stockMoveLine;
      } else {
        TrackingNumberConfiguration trackingNumberConfiguration =
            (TrackingNumberConfiguration)
                productCompanyService.get(
                    product,
                    "trackingNumberConfiguration",
                    Optional.ofNullable(stockMoveLine.getStockMove())
                        .map(StockMove::getCompany)
                        .orElse(null));

        return assignOrGenerateTrackingNumber(
            stockMoveLine, stockMove, product, trackingNumberConfiguration, type);
      }

    } else {
      return this.createStockMoveLine(
          product,
          productName,
          description,
          quantity,
          BigDecimal.ZERO,
          BigDecimal.ZERO,
          companyUnitPriceUntaxed,
          BigDecimal.ZERO,
          unit,
          stockMove,
          null,
          fromStockLocation,
          toStockLocation);
    }
  }

  protected StockMoveLine generateStockMoveLineConvertingUnitPrice(
      Product product,
      String productName,
      String description,
      BigDecimal quantity,
      BigDecimal unitPrice,
      BigDecimal companyUnitPriceUntaxed,
      BigDecimal companyPurchasePrice,
      Unit unit,
      StockMove stockMove,
      boolean taxed,
      BigDecimal taxRate,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {
    BigDecimal unitPriceUntaxed;
    BigDecimal unitPriceTaxed;
    if (taxed) {
      unitPriceTaxed =
          unitPrice.setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
      unitPriceUntaxed =
          unitPrice.divide(
              taxRate.divide(new BigDecimal(100)).add(BigDecimal.ONE),
              appBaseService.getNbDecimalDigitForUnitPrice(),
              RoundingMode.HALF_UP);
    } else {
      unitPriceUntaxed =
          unitPrice.setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
      unitPriceTaxed =
          unitPrice
              .multiply(taxRate.divide(new BigDecimal(100)).add(BigDecimal.ONE))
              .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
    }
    return this.createStockMoveLine(
        product,
        productName,
        description,
        quantity,
        unitPriceUntaxed,
        unitPriceTaxed,
        companyUnitPriceUntaxed,
        companyPurchasePrice,
        unit,
        stockMove,
        null,
        fromStockLocation,
        toStockLocation);
  }

  @Override
  public StockMoveLine assignOrGenerateTrackingNumber(
      StockMoveLine stockMoveLine,
      StockMove stockMove,
      Product product,
      TrackingNumberConfiguration trackingNumberConfiguration,
      int type)
      throws AxelorException {

    if (trackingNumberConfiguration != null) {

      switch (type) {
        case StockMoveLineService.TYPE_SALES:
          if (trackingNumberConfiguration.getIsSaleTrackingManaged()) {
            if (trackingNumberConfiguration.getGenerateSaleAutoTrackingNbr()) {
              // Générer numéro de série si case cochée
              this.generateTrackingNumber(
                  stockMoveLine,
                  trackingNumberConfiguration,
                  product,
                  trackingNumberConfiguration.getSaleQtyByTracking());
            }

            if (trackingNumberConfiguration.getHasSaleAutoSelectTrackingNbr()) {
              // Rechercher le numéro de suivi d'apèrs FIFO/LIFO
              this.assignTrackingNumber(stockMoveLine, product);
            }
          }
          break;
        case StockMoveLineService.TYPE_PURCHASES:
          if (trackingNumberConfiguration.getIsPurchaseTrackingManaged()
              && trackingNumberConfiguration.getGeneratePurchaseAutoTrackingNbr()) {
            // Générer numéro de série si case cochée
            this.generateTrackingNumber(
                stockMoveLine,
                trackingNumberConfiguration,
                product,
                trackingNumberConfiguration.getPurchaseQtyByTracking());
          }
          break;
        case StockMoveLineService.TYPE_OUT_PRODUCTIONS:
          if (trackingNumberConfiguration.getIsProductionTrackingManaged()
              && trackingNumberConfiguration.getGenerateProductionAutoTrackingNbr()) {
            // Générer numéro de série si case cochée
            this.generateTrackingNumber(
                stockMoveLine,
                trackingNumberConfiguration,
                product,
                trackingNumberConfiguration.getProductionQtyByTracking());
          }
          break;
        case StockMoveLineService.TYPE_IN_PRODUCTIONS:
          if (trackingNumberConfiguration.getHasProductAutoSelectTrackingNbr()) {
            // searching for the tracking number using FIFO or LIFO
            this.assignTrackingNumber(stockMoveLine, product);
          }
          break;
        case StockMoveLineService.TYPE_WASTE_PRODUCTIONS:
          break;
        default:
          break;
      }
    }
    return stockMoveLine;
  }

  @Override
  public void generateTrackingNumber(
      StockMoveLine stockMoveLine,
      TrackingNumberConfiguration trackingNumberConfiguration,
      Product product,
      BigDecimal qtyByTracking)
      throws AxelorException {

    int generateTrakingNumberCounter = 0;

    StockMove stockMove = stockMoveLine.getStockMove();

    if (qtyByTracking.compareTo(BigDecimal.ZERO) <= 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.STOCK_MOVE_QTY_BY_TRACKING));
    }
    Partner supplier =
        stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING
                && !stockMove.getIsReversion()
            ? stockMove.getPartner()
            : null;
    while (stockMoveLine.getQty().compareTo(qtyByTracking) > 0) {

      BigDecimal minQty = stockMoveLine.getQty().min(qtyByTracking);

      StockMoveLine newStockMoveLine =
          this.splitStockMoveLine(
              stockMoveLine,
              minQty,
              trackingNumberCreateService.getTrackingNumber(
                  product,
                  stockMove.getCompany(),
                  stockMove.getEstimatedDate(),
                  stockMove.getOrigin(),
                  supplier));
      this.fillOriginTrackingNumber(newStockMoveLine);

      generateTrakingNumberCounter++;

      if (generateTrakingNumberCounter == 1000) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(StockExceptionMessage.STOCK_MOVE_TOO_MANY_ITERATION));
      }
    }
    if (stockMoveLine.getTrackingNumber() == null) {
      stockMoveLine.setTrackingNumber(
          trackingNumberCreateService.getTrackingNumber(
              product,
              stockMove.getCompany(),
              stockMove.getEstimatedDate(),
              stockMove.getOrigin(),
              supplier));
      fillOriginTrackingNumber(stockMoveLine);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void fillOriginTrackingNumber(StockMoveLine stockMoveLine) {
    TrackingNumber trackingNumber = stockMoveLine.getTrackingNumber();
    if (trackingNumber != null) {
      trackingNumber.setOriginMoveTypeSelect(0);
      trackingNumber.setOriginStockMoveLine(stockMoveLine);

      trackingNumberRepo.save(trackingNumber);
    }
  }

  @Override
  public StockMoveLine createStockMoveLine(
      Product product,
      String productName,
      String description,
      BigDecimal quantity,
      BigDecimal unitPriceUntaxed,
      BigDecimal unitPriceTaxed,
      BigDecimal companyUnitPriceUntaxed,
      BigDecimal companyPurchasePrice,
      Unit unit,
      StockMove stockMove,
      TrackingNumber trackingNumber,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {

    StockMoveLine stockMoveLine = new StockMoveLine();
    stockMoveLine.setProduct(product);
    stockMoveLine.setProductName(productName);
    stockMoveLine.setDescription(description);
    stockMoveLine.setQty(quantity);

    stockMoveLine.setUnitPriceUntaxed(unitPriceUntaxed);
    stockMoveLine.setUnitPriceTaxed(unitPriceTaxed);
    stockMoveLine.setUnit(unit);
    stockMoveLine.setTrackingNumber(trackingNumber);
    stockMoveLine.setCompanyUnitPriceUntaxed(companyUnitPriceUntaxed);
    stockMoveLine.setCompanyPurchasePrice(companyPurchasePrice);
    stockMoveLine.setFromStockLocation(fromStockLocation);
    stockMoveLine.setToStockLocation(toStockLocation);
    this.fillRealQuantities(stockMoveLine, stockMove, stockMoveLine.getQty());
    if (fromStockLocation == null) {
      stockMoveLine.setFromStockLocation(stockMove.getFromStockLocation());
    }
    if (toStockLocation == null) {
      stockMoveLine.setToStockLocation(stockMove.getToStockLocation());
    }

    if (stockMove != null) {
      stockMove.addStockMoveLineListItem(stockMoveLine);
      stockMoveLine.setNetMass(
          this.computeNetMass(stockMove, stockMoveLine, stockMove.getCompany()));
      stockMoveLine.setSequence(stockMove.getStockMoveLineList().size());
    } else {
      stockMoveLine.setNetMass(this.computeNetMass(stockMove, stockMoveLine, null));
    }

    stockMoveLine.setTotalNetMass(
        stockMoveLine
            .getRealQty()
            .multiply(stockMoveLine.getNetMass())
            .setScale(2, RoundingMode.HALF_UP));

    if (product != null) {
      stockMoveLine.setCountryOfOrigin(product.getCountryOfOrigin());
      stockMoveLine.setProductTypeSelect(product.getProductTypeSelect());
    }

    return stockMoveLine;
  }

  @Override
  public void assignTrackingNumber(StockMoveLine stockMoveLine, Product product)
      throws AxelorException {

    List<? extends StockLocationLine> stockLocationLineList =
        this.getStockLocationLines(product, stockMoveLine.getFromStockLocation());

    if (stockLocationLineList != null) {
      for (StockLocationLine stockLocationLine : stockLocationLineList) {

        BigDecimal qty = stockLocationLine.getFutureQty();
        if (stockMoveLine.getQty().compareTo(qty) > 0) {
          this.splitStockMoveLine(stockMoveLine, qty, stockLocationLine.getTrackingNumber());
        } else {
          stockMoveLine.setTrackingNumber(stockLocationLine.getTrackingNumber());
          break;
        }
      }
    }
  }

  @Override
  public List<? extends StockLocationLine> getStockLocationLines(
      Product product, StockLocation stockLocation) throws AxelorException {
    TrackingNumberConfiguration trackingNumberConfiguration =
        (TrackingNumberConfiguration)
            productCompanyService.get(
                product,
                "trackingNumberConfiguration",
                Optional.of(stockLocation.getCompany()).orElse(null));
    List<? extends StockLocationLine> stockLocationLineList =
        Beans.get(StockLocationLineRepository.class)
            .all()
            .filter(
                "self.product = ?1 AND self.futureQty > 0 AND self.trackingNumber IS NOT NULL AND self.detailsStockLocation = ?2"
                    + trackingNumberService.getOrderMethod(trackingNumberConfiguration),
                product,
                stockLocation)
            .fetch();

    return stockLocationLineList;
  }

  @Override
  public StockMoveLine splitStockMoveLine(
      StockMoveLine stockMoveLine, BigDecimal qty, TrackingNumber trackingNumber)
      throws AxelorException {

    StockMoveLine newStockMoveLine =
        this.createStockMoveLine(
            stockMoveLine.getProduct(),
            stockMoveLine.getProductName(),
            stockMoveLine.getDescription(),
            qty,
            stockMoveLine.getUnitPriceUntaxed(),
            stockMoveLine.getUnitPriceTaxed(),
            stockMoveLine.getCompanyUnitPriceUntaxed(),
            stockMoveLine.getCompanyPurchasePrice(),
            stockMoveLine.getUnit(),
            stockMoveLine.getStockMove(),
            trackingNumber,
            stockMoveLine.getFromStockLocation(),
            stockMoveLine.getToStockLocation());

    stockMoveLine.setQty(stockMoveLine.getQty().subtract(qty));

    this.fillRealQuantities(
        stockMoveLine, stockMoveLine.getStockMove(), stockMoveLine.getRealQty().subtract(qty));

    return newStockMoveLine;
  }

  @Override
  public void updateLocations(
      int fromStatus,
      int toStatus,
      List<StockMoveLine> stockMoveLineList,
      LocalDate lastFutureStockMoveDate,
      boolean realQty,
      boolean generateOrder)
      throws AxelorException {

    updateLocations(
        fromStatus,
        toStatus,
        stockMoveLineList,
        lastFutureStockMoveDate,
        realQty,
        null,
        null,
        generateOrder);
  }

  @Override
  public void updateLocations(
      int fromStatus,
      int toStatus,
      List<StockMoveLine> stockMoveLineList,
      LocalDate lastFutureStockMoveDate,
      boolean realQty,
      LocalDate date,
      String origin,
      boolean generateOrder)
      throws AxelorException {

    stockMoveLineList = MoreObjects.firstNonNull(stockMoveLineList, Collections.emptyList());

    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      StockLocation fromStockLocation = stockMoveLine.getFromStockLocation();
      StockLocation toStockLocation = stockMoveLine.getToStockLocation();

      Product product = stockMoveLine.getProduct();

      if (product != null
          && product.getProductTypeSelect().equals(ProductRepository.PRODUCT_TYPE_STORABLE)) {

        BigDecimal qty;
        if (realQty) {
          qty = stockMoveLine.getRealQty();
        } else {
          qty = stockMoveLine.getQty();
        }

        this.updateLocations(
            stockMoveLine,
            stockMoveLine.getProduct(),
            qty,
            fromStatus,
            toStatus,
            lastFutureStockMoveDate,
            stockMoveLine.getTrackingNumber(),
            generateOrder);
        if (toStatus == StockMoveRepository.STATUS_REALIZED) {

          if (fromStockLocation.getTypeSelect() != StockLocationRepository.TYPE_VIRTUAL) {
            // We dont recompute average price for outgoing lines
            this.updateStockLocationLineHistory(
                fromStockLocation, stockMoveLine, date, origin, toStatus);
          }
          if (toStockLocation.getTypeSelect() != StockLocationRepository.TYPE_VIRTUAL) {
            this.updateAveragePriceAndLocationLineHistory(
                toStockLocation, stockMoveLine, fromStatus, toStatus, date, origin);
          }
          weightedAveragePriceService.computeAvgPriceForProduct(stockMoveLine.getProduct());
        }
        if (fromStatus == StockMoveRepository.STATUS_REALIZED
            && toStatus == StockMoveRepository.STATUS_CANCELED) {
          // We dont recompute on cancel
          if (fromStockLocation.getTypeSelect() != StockLocationRepository.TYPE_VIRTUAL) {
            this.updateStockLocationLineHistory(
                fromStockLocation, stockMoveLine, date, origin, toStatus);
          }
          if (toStockLocation.getTypeSelect() != StockLocationRepository.TYPE_VIRTUAL) {
            this.updateStockLocationLineHistory(
                toStockLocation, stockMoveLine, date, origin, toStatus);
          }
        }
      }
    }
  }

  @Override
  public void updateAveragePriceAndLocationLineHistory(
      StockLocation stockLocation,
      StockMoveLine stockMoveLine,
      int fromStatus,
      int toStatus,
      LocalDate date,
      String origin)
      throws AxelorException {
    StockLocationLine stockLocationLine =
        stockLocationLineService.getOrCreateStockLocationLine(
            stockLocation, stockMoveLine.getProduct());
    if (stockLocationLine == null) {
      return;
    }

    if (toStatus == StockMoveRepository.STATUS_REALIZED) {
      BigDecimal avgPrice =
          this.computeNewAveragePriceLocationLine(stockLocationLine, stockMoveLine);
      setAvgPriceAndComputeForProduct(stockLocationLine, avgPrice);

      stockLocationLineService.updateHistory(
          stockLocationLine,
          stockMoveLine,
          date != null ? date.atStartOfDay() : null,
          origin,
          getStockLocationLineHistoryTypeSelect(toStatus));
    }
  }

  /**
   * Method that return proper stockLocationLineHistory.typeSelect depending of the statusStockMove
   *
   * @param statusStockMove
   * @return typeSelect matching with statusStockMove
   */
  protected String getStockLocationLineHistoryTypeSelect(int statusStockMove) {
    switch (statusStockMove) {
      case StockMoveRepository.STATUS_CANCELED:
        return StockLocationLineHistoryRepository.TYPE_SELECT_CANCELATION;
      case StockMoveRepository.STATUS_REALIZED:
        return StockLocationLineHistoryRepository.TYPE_SELECT_STOCK_MOVE;
      default:
        return null;
    }
  }

  protected void updateStockLocationLineHistory(
      StockLocation stockLocation,
      StockMoveLine stockMoveLine,
      LocalDate date,
      String origin,
      int toStatus)
      throws AxelorException {
    StockLocationLine stockLocationLine =
        stockLocationLineService.getOrCreateStockLocationLine(
            stockLocation, stockMoveLine.getProduct());
    if (stockLocationLine == null) {
      return;
    }

    if (origin == null) {
      origin =
          Optional.ofNullable(stockMoveLine)
              .map(StockMoveLine::getStockMove)
              .map(StockMove::getStockMoveSeq)
              .orElse("");
    }

    if (toStatus == StockMoveRepository.STATUS_CANCELED) {
      resetAvgPrice(stockLocationLine, origin);
    } else {
      setAvgPriceAndComputeForProduct(
          stockLocationLine,
          Optional.ofNullable(stockLocationLine.getAvgPrice()).orElse(BigDecimal.ZERO));
    }

    stockLocationLineService.updateHistory(
        stockLocationLine,
        stockMoveLine,
        date != null ? date.atStartOfDay() : null,
        origin,
        getStockLocationLineHistoryTypeSelect(toStatus));
  }

  protected void resetAvgPrice(StockLocationLine stockLocationLine, String origin)
      throws AxelorException {

    // Sort by date.
    List<StockLocationLineHistory> sortedHistoryLines =
        stockLocationLineHistoryService.getStockLineHistoryLines(stockLocationLine).stream()
            .sorted((slh1, slh2) -> slh2.getDateT().compareTo(slh1.getDateT()))
            .collect(Collectors.toList());

    if (sortedHistoryLines.size() >= 2 && origin != null) {
      int i = 0;
      StockLocationLineHistory lastHistoryLine = sortedHistoryLines.get(i);

      while (lastHistoryLine != null
          && origin.equals(lastHistoryLine.getOrigin())
          && lastHistoryLine
              .getTypeSelect()
              .equals(StockLocationLineHistoryRepository.TYPE_SELECT_STOCK_MOVE)) {

        i++;
        if (i >= sortedHistoryLines.size()) {
          lastHistoryLine = null;
        } else {
          lastHistoryLine = sortedHistoryLines.get(i);
        }
      }
      // Case where firstHistoryLine is simply not from same origin.
      // Or we could not find historyLine from different origin.
      if (i == 0 || i >= sortedHistoryLines.size()) {
        setAvgPriceAndComputeForProduct(
            stockLocationLine,
            Optional.ofNullable(stockLocationLine.getAvgPrice()).orElse(BigDecimal.ZERO));
      } else {
        setAvgPriceAndComputeForProduct(stockLocationLine, lastHistoryLine.getWap());
      }

    } else {
      setAvgPriceAndComputeForProduct(
          stockLocationLine,
          Optional.ofNullable(stockLocationLine.getAvgPrice()).orElse(BigDecimal.ZERO));
    }
  }

  @Override
  public BigDecimal computeNewAveragePriceLocationLine(
      StockLocationLine stockLocationLine, StockMoveLine stockMoveLine) throws AxelorException {
    BigDecimal oldAvgPrice = stockLocationLine.getAvgPrice();
    // avgPrice in stock move line is a bigdecimal but is nullable.
    BigDecimal newQty = stockMoveLine.getRealQty();
    BigDecimal newPrice =
        stockMoveLine.getWapPrice() != null
            ? stockMoveLine.getWapPrice()
            : stockMoveLine.getCompanyUnitPriceUntaxed();
    BigDecimal newAvgPrice;

    Unit stockLocationLineUnit = stockLocationLine.getUnit();
    Unit stockMoveLineUnit = stockMoveLine.getUnit();

    if (stockLocationLineUnit != null && !stockLocationLineUnit.equals(stockMoveLineUnit)) {
      newQty =
          unitConversionService.convert(
              stockMoveLineUnit,
              stockLocationLineUnit,
              newQty,
              newQty.scale(),
              stockMoveLine.getProduct());

      newPrice =
          unitConversionService.convert(
              stockLocationLineUnit,
              stockMoveLineUnit,
              newPrice,
              newPrice.scale(),
              stockMoveLine.getProduct());
    }

    BigDecimal oldQty = stockLocationLine.getCurrentQty().subtract(newQty);

    log.debug(
        "Old price: {}, Old quantity: {}, New price: {}, New quantity: {}",
        oldAvgPrice,
        oldQty,
        newPrice,
        newQty);
    BigDecimal sum = oldAvgPrice.multiply(oldQty);
    sum = sum.add(newPrice.multiply(newQty));
    BigDecimal denominator = oldQty.add(newQty);
    if (denominator.compareTo(BigDecimal.ZERO) != 0) {
      int scale = appBaseService.getNbDecimalDigitForUnitPrice();
      newAvgPrice = sum.divide(denominator, scale, RoundingMode.HALF_UP);
    } else {
      newAvgPrice = oldAvgPrice;
    }
    return newAvgPrice;
  }

  protected void setAvgPriceAndComputeForProduct(
      StockLocationLine stockLocationLine, BigDecimal avgPrice) throws AxelorException {
    stockLocationLine.setAvgPrice(avgPrice);
    weightedAveragePriceService.computeAvgPriceForProduct(stockLocationLine.getProduct());
  }

  @Override
  public void checkConformitySelection(StockMoveLine stockMoveLine, StockMove stockMove)
      throws AxelorException {
    Product product = stockMoveLine.getProduct();
    // check if the product configuration forces to select a conformity
    if (product == null) {
      return;
    }
    Boolean controlOnReceipt =
        (Boolean) productCompanyService.get(product, "controlOnReceipt", stockMove.getCompany());
    // check the stock move type
    if (!controlOnReceipt || stockMove.getTypeSelect() != StockMoveRepository.TYPE_INCOMING) {
      return;
    }

    // check the conformity
    if (stockMoveLine.getConformitySelect() <= StockMoveLineRepository.CONFORMITY_NONE) {
      throw new AxelorException(
          stockMoveLine,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.STOCK_MOVE_LINE_MUST_FILL_CONFORMITY),
          product.getName());
    }
  }

  @Override
  public void checkConformitySelection(StockMove stockMove) throws AxelorException {

    if (!appStockService.getAppStock().getRequireToFillConformityOnReceipt()) {
      return;
    }

    List<String> productsWithErrors = new ArrayList<>();
    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {

      Product product = stockMoveLine.getProduct();

      if (product != null
          && ((String)
                  productCompanyService.get(product, "productTypeSelect", stockMove.getCompany()))
              .equals(ProductRepository.PRODUCT_TYPE_STORABLE)) {
        try {
          checkConformitySelection(stockMoveLine, stockMove);
        } catch (Exception e) {
          productsWithErrors.add(
              (String) productCompanyService.get(product, "name", stockMove.getCompany()));
        }
      }
    }
    if (!productsWithErrors.isEmpty()) {
      String productsWithErrorStr = productsWithErrors.stream().collect(Collectors.joining(", "));
      throw new AxelorException(
          stockMove,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.STOCK_MOVE_LINE_MUST_FILL_CONFORMITY),
          productsWithErrorStr);
    }
  }

  @Override
  public void checkExpirationDates(StockMove stockMove) {
    List<String> errorList = new ArrayList<>();

    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
      TrackingNumber trackingNumber = stockMoveLine.getTrackingNumber();
      if (trackingNumber == null
          || !trackingNumber.getCheckExpirationDateAtStockMoveRealization()) {
        continue;
      }
      Product product = trackingNumber.getProduct();

      if (trackingNumber.getHasWarranty()
              && trackingNumber
                  .getWarrantyExpirationDate()
                  .isBefore(appBaseService.getTodayDate(stockMove.getCompany()))
          || trackingNumber.getIsPerishable()
              && trackingNumber
                  .getPerishableExpirationDate()
                  .isBefore(appBaseService.getTodayDate(stockMove.getCompany()))) {
        errorList.add(product.getName());
      }
    }

    if (!errorList.isEmpty()) {
      String errorStr = errorList.stream().collect(Collectors.joining(", "));
      TraceBackService.trace(
          new AxelorAlertException(
              stockMove,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(StockExceptionMessage.STOCK_MOVE_LINE_EXPIRED_PRODUCTS),
              errorStr));
    }
  }

  @Override
  public void checkTrackingNumber(StockMove stockMove) throws AxelorException {
    List<String> productsWithErrors = new ArrayList<>();

    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
      if (stockMoveLine.getProduct() == null) {
        continue;
      }

      TrackingNumberConfiguration trackingNumberConfig =
          (TrackingNumberConfiguration)
              productCompanyService.get(
                  stockMoveLine.getProduct(),
                  "trackingNumberConfiguration",
                  stockMove.getCompany());

      if (stockMoveLine.getProduct() != null
          && trackingNumberConfig != null
          && (trackingNumberConfig.getIsPurchaseTrackingManaged()
              || trackingNumberConfig.getIsProductionTrackingManaged()
              || (trackingNumberConfig.getIsSaleTrackingManaged()
                  && stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING))
          && stockMoveLine.getTrackingNumber() == null
          && stockMoveLine.getRealQty().compareTo(BigDecimal.ZERO) != 0) {
        if (!productsWithErrors.contains(stockMoveLine.getProduct().getName())) {
          productsWithErrors.add(stockMoveLine.getProduct().getName());
        }
      }
    }

    if (!productsWithErrors.isEmpty()) {
      String productWithErrorsStr = productsWithErrors.stream().collect(Collectors.joining(", "));
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(StockExceptionMessage.STOCK_MOVE_LINE_MUST_FILL_TRACKING_NUMBER),
          productWithErrorsStr);
    }
  }

  @Override
  public void updateLocations(
      StockMoveLine stockMoveLine,
      Product product,
      BigDecimal qty,
      int fromStatus,
      int toStatus,
      LocalDate lastFutureStockMoveDate,
      TrackingNumber trackingNumber,
      boolean generateOrder)
      throws AxelorException {
    Unit stockMoveLineUnit = stockMoveLine.getUnit();

    switch (fromStatus) {
      case StockMoveRepository.STATUS_PLANNED:
        stockLocationLineService.updateLocation(
            stockMoveLine.getFromStockLocation(),
            product,
            stockMoveLineUnit,
            qty,
            false,
            true,
            true,
            null,
            trackingNumber,
            generateOrder);
        stockLocationLineService.updateLocation(
            stockMoveLine.getToStockLocation(),
            product,
            stockMoveLineUnit,
            qty,
            false,
            true,
            false,
            null,
            trackingNumber,
            generateOrder);
        break;

      case StockMoveRepository.STATUS_REALIZED:
        stockLocationLineService.updateLocation(
            stockMoveLine.getFromStockLocation(),
            product,
            stockMoveLineUnit,
            qty,
            true,
            true,
            true,
            null,
            trackingNumber,
            generateOrder);
        stockLocationLineService.updateLocation(
            stockMoveLine.getToStockLocation(),
            product,
            stockMoveLineUnit,
            qty,
            true,
            true,
            false,
            null,
            trackingNumber,
            generateOrder);
        break;

      default:
        break;
    }

    switch (toStatus) {
      case StockMoveRepository.STATUS_PLANNED:
        stockLocationLineService.updateLocation(
            stockMoveLine.getFromStockLocation(),
            product,
            stockMoveLineUnit,
            qty,
            false,
            true,
            false,
            lastFutureStockMoveDate,
            trackingNumber,
            generateOrder);
        stockLocationLineService.updateLocation(
            stockMoveLine.getToStockLocation(),
            product,
            stockMoveLineUnit,
            qty,
            false,
            true,
            true,
            lastFutureStockMoveDate,
            trackingNumber,
            generateOrder);
        break;

      case StockMoveRepository.STATUS_REALIZED:
        stockLocationLineService.updateLocation(
            stockMoveLine.getFromStockLocation(),
            product,
            stockMoveLineUnit,
            qty,
            true,
            true,
            false,
            null,
            trackingNumber,
            generateOrder);
        stockLocationLineService.updateLocation(
            stockMoveLine.getToStockLocation(),
            product,
            stockMoveLineUnit,
            qty,
            true,
            true,
            true,
            null,
            trackingNumber,
            generateOrder);
        break;

      default:
        break;
    }
  }

  @Override
  public Unit getStockUnit(StockMoveLine stockMoveLine) {
    Unit stockUnit = stockMoveLine.getUnit();
    if (stockUnit == null && stockMoveLine.getProduct() != null) {
      stockUnit = stockMoveLine.getProduct().getUnit();
    }
    return stockUnit;
  }

  @Override
  public StockMoveLine compute(StockMoveLine stockMoveLine, StockMove stockMove)
      throws AxelorException {
    BigDecimal unitPriceUntaxed = BigDecimal.ZERO;
    BigDecimal companyPurchasePrice = BigDecimal.ZERO;
    if (stockMoveLine.getProduct() != null && stockMove != null) {
      if ((stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING
              && stockMove.getIsReversion())
          || (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING
              && !stockMove.getIsReversion())) {
        // customer delivery or customer return
        unitPriceUntaxed =
            (BigDecimal)
                productCompanyService.get(
                    stockMoveLine.getProduct(), "salePrice", stockMove.getCompany());
        BigDecimal wapPrice =
            computeFromStockLocation(stockMoveLine, stockMoveLine.getToStockLocation());
        stockMoveLine.setWapPrice(wapPrice);
      } else if ((stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING
              && stockMove.getIsReversion())
          || (stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING
              && !stockMove.getIsReversion())) {
        // supplier return or supplier delivery
        BigDecimal shippingCoef =
            shippingCoefService.getShippingCoef(
                stockMoveLine.getProduct(),
                stockMove.getPartner(),
                stockMove.getCompany(),
                stockMoveLine.getRealQty());
        companyPurchasePrice =
            (BigDecimal)
                productCompanyService.get(
                    stockMoveLine.getProduct(), "purchasePrice", stockMove.getCompany());
        ;
        unitPriceUntaxed = companyPurchasePrice.multiply(shippingCoef);
      } else if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_INTERNAL
          && stockMoveLine.getFromStockLocation() != null
          && stockMoveLine.getFromStockLocation().getTypeSelect()
              != StockLocationRepository.TYPE_VIRTUAL) {
        unitPriceUntaxed =
            computeFromStockLocation(stockMoveLine, stockMoveLine.getFromStockLocation());
      } else {
        unitPriceUntaxed =
            (BigDecimal)
                productCompanyService.get(
                    stockMoveLine.getProduct(), "costPrice", stockMove.getCompany());
      }
    }
    stockMoveLine.setCompanyPurchasePrice(companyPurchasePrice);
    stockMoveLine.setUnitPriceUntaxed(unitPriceUntaxed);
    stockMoveLine.setUnitPriceTaxed(unitPriceUntaxed);
    stockMoveLine.setCompanyUnitPriceUntaxed(unitPriceUntaxed);
    return stockMoveLine;
  }

  /**
   * Compute the price corresponding to the stock move line in the stock location. The price is the
   * average price in the stock location line with the same product as the stock move line, after
   * converting the unit.
   *
   * @param stockMoveLine a stock move line with a product.
   * @param stockLocation a stock location.
   * @return the computed price.
   * @throws AxelorException if the conversion fails.
   */
  protected BigDecimal computeFromStockLocation(
      StockMoveLine stockMoveLine, StockLocation stockLocation) throws AxelorException {
    Optional<StockLocationLine> stockLocationLine =
        Optional.ofNullable(
            stockLocationLineFetchService.getStockLocationLine(
                stockLocation, stockMoveLine.getProduct()));
    BigDecimal priceFromLocation = BigDecimal.ZERO;
    if (stockLocationLine.isPresent()) {
      priceFromLocation = stockLocationLine.get().getAvgPrice();
      priceFromLocation =
          unitConversionService.convert(
              stockMoveLine.getUnit(),
              getStockUnit(stockMoveLine),
              priceFromLocation,
              priceFromLocation.scale(),
              null);
    }
    return priceFromLocation;
  }

  @Override
  public void storeCustomsCodes(List<StockMoveLine> stockMoveLineList) throws AxelorException {
    if (stockMoveLineList == null) {
      return;
    }

    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      Product product = stockMoveLine.getProduct();
      CustomsCodeNomenclature customsCodeNomenclature =
          product != null
              ? (CustomsCodeNomenclature)
                  productCompanyService.get(
                      product, "customsCodeNomenclature", stockMoveLine.getStockMove().getCompany())
              : null;
      stockMoveLine.setCustomsCodeNomenclature(customsCodeNomenclature);
      stockMoveLine.setCustomsCode(
          customsCodeNomenclature != null ? customsCodeNomenclature.getCode() : null);
    }
  }

  @Override
  public boolean computeFullySpreadOverLogisticalFormLinesFlag(StockMoveLine stockMoveLine) {
    return computeSpreadableQtyOverLogisticalFormLines(stockMoveLine).signum() <= 0;
  }

  @Override
  public BigDecimal computeSpreadableQtyOverLogisticalFormLines(StockMoveLine stockMoveLine) {
    return stockMoveLine != null
        ? computeSpreadableQtyOverLogisticalFormLines(
            stockMoveLine, stockMoveLine.getLogisticalFormLineList())
        : BigDecimal.ZERO;
  }

  @Override
  public BigDecimal computeSpreadableQtyOverLogisticalFormLines(
      StockMoveLine stockMoveLine, LogisticalForm logisticalForm) {

    if (logisticalForm == null && stockMoveLine != null) {
      return computeSpreadableQtyOverLogisticalFormLines(
          stockMoveLine, stockMoveLine.getLogisticalFormLineList());
    }

    List<LogisticalFormLine> updatedLogisticalFormLineList = new ArrayList<>();

    if (stockMoveLine != null && stockMoveLine.getLogisticalFormLineList() != null) {
      for (LogisticalFormLine logisticalFormLine : stockMoveLine.getLogisticalFormLineList()) {
        if (!logisticalForm.equals(logisticalFormLine.getLogisticalForm())) {
          updatedLogisticalFormLineList.add(logisticalFormLine);
        }
      }
    }

    if (logisticalForm.getLogisticalFormLineList() != null) {
      for (LogisticalFormLine logisticalFormLine : logisticalForm.getLogisticalFormLineList()) {
        if (stockMoveLine != null && stockMoveLine.equals(logisticalFormLine.getStockMoveLine())) {
          updatedLogisticalFormLineList.add(logisticalFormLine);
        }
      }
    }

    return computeSpreadableQtyOverLogisticalFormLines(
        stockMoveLine, updatedLogisticalFormLineList);
  }

  protected BigDecimal computeSpreadableQtyOverLogisticalFormLines(
      StockMoveLine stockMoveLine, List<LogisticalFormLine> logisticalFormLineList) {

    if (stockMoveLine == null) {
      return null;
    }

    if (stockMoveLine.getProduct() == null
        || !ProductRepository.PRODUCT_TYPE_STORABLE.equals(
            stockMoveLine.getProduct().getProductTypeSelect())) {
      return BigDecimal.ZERO;
    }

    if (logisticalFormLineList == null) {
      return stockMoveLine.getRealQty();
    }

    BigDecimal qtySpreadOverLogisticalMoveLines =
        logisticalFormLineList.stream()
            .map(
                logisticalFormLine ->
                    logisticalFormLine.getQty() != null
                        ? logisticalFormLine.getQty()
                        : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    return stockMoveLine.getRealQty().subtract(qtySpreadOverLogisticalMoveLines);
  }

  @Override
  public void setProductInfo(StockMove stockMove, StockMoveLine stockMoveLine, Company company)
      throws AxelorException {
    Preconditions.checkNotNull(stockMoveLine);
    Preconditions.checkNotNull(company);
    Product product = stockMoveLine.getProduct();

    if (product == null) {
      return;
    }

    stockMoveLine.setUnit(product.getUnit());
    stockMoveLine.setProductName(product.getName());

    if (appStockService.getAppStock().getIsEnabledProductDescriptionCopy()) {
      stockMoveLine.setDescription(product.getDescription());
    }

    if (appBaseService.getAppBase().getManageProductVariants()) {
      stockMoveLine.setProductModel(product.getParentProduct());
    }

    stockMoveLine.setCountryOfOrigin(product.getCountryOfOrigin());

    BigDecimal netMass = this.computeNetMass(stockMove, stockMoveLine, company);
    stockMoveLine.setNetMass(netMass);
  }

  public BigDecimal computeNetMass(
      StockMove stockMove, StockMoveLine stockMoveLine, Company company) throws AxelorException {

    BigDecimal netMass;
    Product product = stockMoveLine.getProduct();
    Unit startUnit;
    Unit endUnit;

    if (product == null
        || !product.getProductTypeSelect().equals(ProductRepository.PRODUCT_TYPE_STORABLE)) {
      return null;
    }

    startUnit = product.getMassUnit();
    if (startUnit == null) {

      if (stockMove == null || !checkMassesRequired(stockMove, stockMoveLine)) {
        return product.getNetMass();
      }

      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.MISSING_PRODUCT_MASS_UNIT),
          product.getName());
    }

    if (company != null
        && company.getStockConfig() != null
        && company.getStockConfig().getCustomsMassUnit() != null) {
      endUnit = company.getStockConfig().getCustomsMassUnit();
    } else {
      endUnit = startUnit;
    }

    netMass = unitConversionService.convert(startUnit, endUnit, product.getNetMass(), 10, product);
    return netMass;
  }

  @Override
  public boolean checkMassesRequired(StockMove stockMove, StockMoveLine stockMoveLine) {
    Address fromAddress = stockMoveToolService.getFromAddress(stockMove, stockMoveLine);
    Address toAddress = stockMoveToolService.getToAddress(stockMove, stockMoveLine);

    Country fromCountry = fromAddress != null ? fromAddress.getCountry() : null;
    Country toCountry = toAddress != null ? toAddress.getCountry() : null;

    return fromCountry != null
        && toCountry != null
        && !fromCountry.equals(toCountry)
        && stockMoveLine.getProduct() != null
        && fromCountry.getEconomicArea() != null
        && fromCountry.getEconomicArea().equals(toCountry.getEconomicArea())
        && stockMoveLine.getProduct().getUsedInDEB();
  }

  @Override
  @Transactional
  public void splitStockMoveLineByTrackingNumber(
      StockMoveLine stockMoveLine, List<LinkedHashMap<String, Object>> trackingNumbers)
      throws AxelorException {
    //    boolean draft = true;
    //    if (stockMoveLine.getStockMove() != null
    //        && stockMoveLine.getStockMove().getStatusSelect() ==
    // StockMoveRepository.STATUS_PLANNED) {
    //      draft = false;
    //    }
    BigDecimal totalSplitQty = BigDecimal.ZERO;
    for (LinkedHashMap<String, Object> trackingNumberItem : trackingNumbers) {
      BigDecimal counter = new BigDecimal(trackingNumberItem.get("counter").toString());
      if (counter.compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      totalSplitQty = totalSplitQty.add(counter);

      TrackingNumber trackingNumber =
          trackingNumberRepo
              .all()
              .filter(
                  "self.product.id = ?1 and self.trackingNumberSeq = ?2",
                  stockMoveLine.getProduct(),
                  trackingNumberItem.get("trackingNumberSeq").toString())
              .fetchOne();

      if (trackingNumber == null) {
        trackingNumber = new TrackingNumber();
        trackingNumber.setTrackingNumberSeq(trackingNumberItem.get("trackingNumberSeq").toString());
        boolean hasWarranty =
            trackingNumberItem.get("hasWarranty") != null
                && Boolean.parseBoolean(trackingNumberItem.get("hasWarranty").toString());
        trackingNumber.setHasWarranty(hasWarranty);
        if (trackingNumberItem.get("warrantyExpirationDate") != null) {
          trackingNumber.setWarrantyExpirationDate(
              LocalDate.parse(trackingNumberItem.get("warrantyExpirationDate").toString()));
        }
        boolean isPerishable =
            trackingNumberItem.get("isPerishable") != null
                && Boolean.parseBoolean(trackingNumberItem.get("isPerishable").toString());
        trackingNumber.setIsPerishable(isPerishable);
        if (trackingNumberItem.get("perishableExpirationDate") != null) {
          trackingNumber.setPerishableExpirationDate(
              LocalDate.parse(trackingNumberItem.get("perishableExpirationDate").toString()));
        }
        if (trackingNumberItem.get("origin") != null) {
          trackingNumber.setOrigin(trackingNumberItem.get("origin").toString());
        }
        if (trackingNumberItem.get("note") != null) {
          trackingNumber.setNote(trackingNumberItem.get("note").toString());
        }
        if (trackingNumberItem.get("serialNbr") != null) {
          trackingNumber.setSerialNumber(trackingNumberItem.get("serialNbr").toString());
        }
        trackingNumber.setProduct(stockMoveLine.getProduct());

        if (stockMoveLine.getProduct() != null) {
          // In case of barcode generation, retrieve the one set on tracking number configuration
          AppStock appStock = appStockService.getAppStock();
          TrackingNumberConfiguration trackingNumberConfiguration =
              (TrackingNumberConfiguration)
                  productCompanyService.get(
                      stockMoveLine.getProduct(),
                      "trackingNumberConfiguration",
                      Optional.ofNullable(stockMoveLine.getStockMove())
                          .map(StockMove::getCompany)
                          .orElse(null));
          if (appStock != null
              && appStock.getActivateTrackingNumberBarCodeGeneration()
              && trackingNumberConfiguration != null) {
            if (appStock.getEditTrackingNumberBarcodeType()) {
              trackingNumber.setBarcodeTypeConfig(
                  trackingNumberConfiguration.getBarcodeTypeConfig());
            } else {
              trackingNumber.setBarcodeTypeConfig(appStock.getTrackingNumberBarcodeTypeConfig());
            }
            if (trackingNumberConfiguration.getUseTrackingNumberSeqAsSerialNbr()) {
              trackingNumber.setSerialNumber(trackingNumber.getTrackingNumberSeq());
            }
            // It will launch barcode generation
            trackingNumberRepo.save(trackingNumber);
          }
        }
      }

      StockMoveLine newStockMoveLine = stockMoveLineRepository.copy(stockMoveLine, true);
      //      if (draft) {
      newStockMoveLine.setQty(counter);
      //      } else {
      newStockMoveLine.setRealQty(counter);
      //      }
      newStockMoveLine.setTrackingNumber(trackingNumber);
      newStockMoveLine.setStockMove(stockMoveLine.getStockMove());
      stockMoveLineRepository.save(newStockMoveLine);
    }

    if (totalSplitQty.compareTo(stockMoveLine.getQty()) < 0) {
      BigDecimal remainingQty = stockMoveLine.getQty().subtract(totalSplitQty);
      stockMoveLine.setQty(remainingQty);
      stockMoveLine.setRealQty(remainingQty);
      stockMoveLine.setTrackingNumber(null);
      stockMoveLine.setStockMove(stockMoveLine.getStockMove());
    } else {
      stockMoveLineRepository.remove(stockMoveLine);
    }
  }

  @Override
  public void updateAvailableQty(StockMoveLine stockMoveLine, StockLocation stockLocation)
      throws AxelorException {
    BigDecimal availableQty = BigDecimal.ZERO;
    BigDecimal availableQtyForProduct = BigDecimal.ZERO;

    TrackingNumberConfiguration trackingNumberConfiguration =
        (TrackingNumberConfiguration)
            productCompanyService.get(
                stockMoveLine.getProduct(),
                "trackingNumberConfiguration",
                Optional.ofNullable(stockMoveLine.getStockMove())
                    .map(StockMove::getCompany)
                    .orElse(null));

    if (stockMoveLine.getProduct() != null) {
      if (trackingNumberConfiguration != null) {

        if (stockMoveLine.getTrackingNumber() != null) {
          StockLocationLine stockLocationLine =
              stockLocationLineFetchService.getDetailLocationLine(
                  stockLocation, stockMoveLine.getProduct(), stockMoveLine.getTrackingNumber());

          if (stockLocationLine != null) {
            availableQty = stockLocationLine.getCurrentQty();
          }
        }

        if (availableQty.compareTo(stockMoveLine.getRealQty()) < 0) {
          StockLocationLine stockLocationLineForProduct =
              stockLocationLineFetchService.getStockLocationLine(
                  stockLocation, stockMoveLine.getProduct());

          if (stockLocationLineForProduct != null) {
            availableQtyForProduct = stockLocationLineForProduct.getCurrentQty();
          }
        }
      } else {
        StockLocationLine stockLocationLine =
            stockLocationLineFetchService.getStockLocationLine(
                stockLocation, stockMoveLine.getProduct());

        if (stockLocationLine != null) {
          availableQty = stockLocationLine.getCurrentQty();
        }
      }
    }
    stockMoveLine.setAvailableQty(availableQty);
    stockMoveLine.setAvailableQtyForProduct(availableQtyForProduct);
  }

  @Override
  public String createDomainForProduct(StockMoveLine stockMoveLine, StockMove stockMove)
      throws AxelorException {
    String domain = getCommonFilter(stockMoveLine);
    domain += getProductTypeFilter(stockMoveLine, stockMove);
    return domain;
  }

  protected String getCommonFilter(StockMoveLine stockMoveLine) {
    String domain = "self.isModel = false AND self.isShippingCostsProduct = false";
    if (stockMoveLine.getProductModel() != null) {
      domain += " AND self.parentProduct.id = " + stockMoveLine.getProductModel().getId();
    }
    domain += " AND self.dtype = 'Product'";
    return domain;
  }

  protected String getProductTypeFilter(StockMoveLine stockMoveLine, StockMove stockMove)
      throws AxelorException {
    return getFilterForStorables(stockMoveLine, stockMove);
  }

  protected String getFilterForStorables(StockMoveLine stockMoveLine, StockMove stockMove)
      throws AxelorException {
    if (stockMoveLine.getFilterOnAvailableProducts()
        && stockMoveLine.getFromStockLocation() != null
        && stockMoveLine.getFromStockLocation().getTypeSelect() != 3) {
      return " AND self.id in (select sll.product.id from StockLocation sl inner join sl.stockLocationLineList sll WHERE sl.id = "
          + stockMoveLine.getFromStockLocation().getId()
          + " AND sll.currentQty > 0)";
    }
    return "";
  }

  @Override
  public Map<String, Object> setAvailableStatus(StockMoveLine stockMoveLine)
      throws AxelorException {
    if (stockMoveLine.getStockMove() != null) {
      this.updateAvailableQty(stockMoveLine, stockMoveLine.getFromStockLocation());
    }
    Map<String, Object> availabilityMap = new HashMap<>();
    if (stockMoveLine.getProduct() != null
        && !stockMoveLine
            .getProduct()
            .getProductTypeSelect()
            .equals(ProductRepository.PRODUCT_TYPE_SERVICE)) {
      BigDecimal availableQty = stockMoveLine.getAvailableQty();
      BigDecimal availableQtyForProduct = stockMoveLine.getAvailableQtyForProduct();
      BigDecimal realQty = stockMoveLine.getRealQty();

      if (availableQty.compareTo(realQty) >= 0 || !stockMoveLine.getProduct().getStockManaged()) {
        stockMoveLine.setAvailableStatus(I18n.get("Available"));
        stockMoveLine.setAvailableStatusSelect(StockMoveLineRepository.STATUS_AVAILABLE);
        availabilityMap.put("availability", stockMoveLine.getAvailableStatus());
      } else if (availableQtyForProduct.compareTo(realQty) >= 0) {
        stockMoveLine.setAvailableStatus(I18n.get("Av. for product"));
        stockMoveLine.setAvailableStatusSelect(
            StockMoveLineRepository.STATUS_AVAILABLE_FOR_PRODUCT);
        availabilityMap.put("availability", stockMoveLine.getAvailableStatus());
      } else if (availableQty.compareTo(realQty) < 0
          && availableQtyForProduct.compareTo(realQty) < 0) {
        BigDecimal missingQty = computeMissingQty(stockMoveLine);
        stockMoveLine.setAvailableStatus(I18n.get("Missing") + " (" + missingQty + ")");
        stockMoveLine.setAvailableStatusSelect(StockMoveLineRepository.STATUS_MISSING);
        availabilityMap.put("availability", I18n.get("Missing"));
        availabilityMap.put("missingQty", missingQty);
      }
    }
    return availabilityMap;
  }

  protected BigDecimal computeMissingQty(StockMoveLine stockMoveLine) throws AxelorException {
    TrackingNumberConfiguration trackingNumberConfiguration =
        (TrackingNumberConfiguration)
            productCompanyService.get(
                stockMoveLine.getProduct(),
                "trackingNumberConfiguration",
                Optional.ofNullable(stockMoveLine.getStockMove())
                    .map(StockMove::getCompany)
                    .orElse(null));

    if (trackingNumberConfiguration != null) {
      return stockMoveLine.getAvailableQtyForProduct().subtract(stockMoveLine.getRealQty());
    } else {
      return stockMoveLine.getAvailableQty().subtract(stockMoveLine.getRealQty());
    }
  }

  @Override
  public List<TrackingNumber> getAvailableTrackingNumbers(StockMoveLine stockMoveLine) {
    String domain =
        "self.product.id = :productId"
            + " AND (self.id in (select stockLocationLine.trackingNumber.id from StockLocationLine stockLocationLine"
            + " join StockLocation sl on sl.id = stockLocationLine.detailsStockLocation.id WHERE sl.id = :fromStockLocationId"
            + " AND coalesce(stockLocationLine.currentQty, 0) != 0)"
            + " OR self.id not in (select stockLocationLine.trackingNumber.id from StockLocationLine stockLocationLine"
            + " join StockLocation sl on sl.id = stockLocationLine.detailsStockLocation.id))";
    return trackingNumberRepo
        .all()
        .filter(domain)
        .bind("productId", stockMoveLine.getProduct().getId())
        .bind("fromStockLocationId", stockMoveLine.getFromStockLocation().getId())
        .fetch();
  }

  public void fillRealizeWapPrice(StockMoveLine stockMoveLine) {
    StockLocation stockLocation = stockMoveLine.getFromStockLocation();
    if (stockLocation.getTypeSelect() != StockLocationRepository.TYPE_VIRTUAL) {
      Optional<StockLocationLine> stockLocationLineOpt =
          Optional.ofNullable(
              stockLocationLineFetchService.getStockLocationLine(
                  stockLocation, stockMoveLine.getProduct()));

      stockLocationLineOpt.ifPresent(
          stockLocationLine -> stockMoveLine.setWapPrice(stockLocationLine.getAvgPrice()));
    }
  }

  /** Create new stock line, then set product infos and compute prices (API AOS) */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public StockMoveLine createStockMoveLine(
      StockMove stockMove,
      Product product,
      TrackingNumber trackingNumber,
      BigDecimal qty,
      BigDecimal realQty,
      Unit unit,
      Integer conformitySelect,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {

    StockMoveLine line =
        createStockMoveLine(
            product,
            product.getName(),
            "",
            qty,
            null,
            null,
            null,
            null,
            unit,
            stockMove,
            trackingNumber,
            fromStockLocation,
            toStockLocation);
    setProductInfo(stockMove, line, stockMove.getCompany());
    compute(line, stockMove);
    line.setRealQty(realQty);
    line.setConformitySelect(conformitySelect);
    line.setIsRealQtyModifiedByUser(true);
    stockMoveLineRepository.save(line);
    return line;
  }

  /** Update stock move line realQty and conformity (API AOS) */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateStockMoveLine(
      StockMoveLine stockMoveLine,
      BigDecimal realQty,
      Integer conformity,
      Unit unit,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {
    if (stockMoveLine.getStockMove() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, "Error: missing parent stock move.");
    } else {
      if (stockMoveLine.getStockMove().getStatusSelect() != StockMoveRepository.STATUS_REALIZED
          && stockMoveLine.getStockMove().getStatusSelect()
              != StockMoveRepository.STATUS_CANCELED) {
        stockMoveLine.setRealQty(realQty);
        stockMoveLine.setConformitySelect(conformity);
        stockMoveLine.setIsRealQtyModifiedByUser(true);

        updateUnit(stockMoveLine, unit);
        if (fromStockLocation != null) {
          stockMoveLine.setFromStockLocation(fromStockLocation);
        }
        if (toStockLocation != null) {
          stockMoveLine.setToStockLocation(toStockLocation);
        }
      }
    }
  }

  protected void updateUnit(StockMoveLine stockMoveLine, Unit unit) throws AxelorException {
    if (unit != null) {
      BigDecimal convertQty =
          unitConversionService.convert(
              stockMoveLine.getUnit(),
              unit,
              stockMoveLine.getQty(),
              stockMoveLine.getQty().scale(),
              stockMoveLine.getProduct());
      stockMoveLine.setUnit(unit);
      stockMoveLine.setQty(convertQty);
    }
  }

  @Override
  public StockMoveLine resetStockMoveLine(StockMoveLine stockMoveLine) {
    if (stockMoveLine != null) {

      stockMoveLine.setPlannedStockMove(null);
      stockMoveLine.setProduct(null);
      stockMoveLine.setFilterOnAvailableProducts(true);
      stockMoveLine.setQty(BigDecimal.ZERO);
      stockMoveLine.setRealQty(BigDecimal.ZERO);
      stockMoveLine.setOldQty(BigDecimal.ZERO);
      stockMoveLine.setAvailableQty(BigDecimal.ZERO);
      stockMoveLine.setAvailableQtyForProduct(BigDecimal.ZERO);
      stockMoveLine.setAvailableStatus(null);
      stockMoveLine.setUnit(null);
      stockMoveLine.setNetMass(BigDecimal.ZERO);
      stockMoveLine.setTotalNetMass(BigDecimal.ZERO);
      stockMoveLine.setTrackingNumber(null);
      stockMoveLine.setConformitySelect(null);
      stockMoveLine.setShippedQty(BigDecimal.ZERO);
      stockMoveLine.setShippedDate(null);
      stockMoveLine.setProductModel(null);
      stockMoveLine.setProductName(null);
      stockMoveLine.setDescription(null);
      stockMoveLine.setUnitPriceTaxed(BigDecimal.ZERO);
      stockMoveLine.setUnitPriceUntaxed(BigDecimal.ZERO);
      stockMoveLine.setCompanyUnitPriceUntaxed(BigDecimal.ZERO);
      stockMoveLine.setWapPrice(BigDecimal.ZERO);
      stockMoveLine.setCompanyPurchasePrice(BigDecimal.ZERO);
      stockMoveLine.setProductTypeSelect(null);
      stockMoveLine.setSequence(null);
      stockMoveLine.setName(null);
      stockMoveLine.setCustomsCodeNomenclature(null);
      stockMoveLine.setCustomsCode(null);
      stockMoveLine.setLogisticalFormLineList(null);
      stockMoveLine.setLineTypeSelect(null);
      stockMoveLine.setRegime(null);
      stockMoveLine.setNatureOfTransaction(null);
      stockMoveLine.setCountryOfOrigin(null);
      stockMoveLine.setIsRealQtyModifiedByUser(false);
    }
    return stockMoveLine;
  }

  @Override
  public Map<String, Object> getClearedStockMoveLineMap() {
    Map<String, Object> clearedStockMoveLineMap = new HashMap<>();
    clearedStockMoveLineMap.put("product", null);
    clearedStockMoveLineMap.put("productName", "");
    clearedStockMoveLineMap.put("qty", BigDecimal.ZERO);
    clearedStockMoveLineMap.put("realQty", BigDecimal.ZERO);
    clearedStockMoveLineMap.put("unitPriceUntaxed", BigDecimal.ZERO);
    clearedStockMoveLineMap.put("qtyInvoiced", BigDecimal.ZERO);
    clearedStockMoveLineMap.put("companyPurchasePrice", BigDecimal.ZERO);
    clearedStockMoveLineMap.put("reservedQty", BigDecimal.ZERO);
    clearedStockMoveLineMap.put("unit", null);
    clearedStockMoveLineMap.put("netMass", BigDecimal.ZERO);
    clearedStockMoveLineMap.put("totalNetMass", BigDecimal.ZERO);
    clearedStockMoveLineMap.put("trackingNumber", null);
    return clearedStockMoveLineMap;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void splitStockMoveLineByTrackingNumber(StockMove stockMove) throws AxelorException {
    Integer type = stockMove.getTypeSelect();
    List<StockMoveLine> stockMoveLineList = stockMove.getStockMoveLineList();
    if (type == StockMoveRepository.TYPE_INTERNAL || CollectionUtils.isEmpty(stockMoveLineList)) {
      return;
    }
    // Does not manage the case where line is already splited
    // Works when generating a tracking number
    // But not when assigning one
    for (StockMoveLine stockMoveLine : new CopyOnWriteArrayList<>(stockMoveLineList)) {
      Product product = stockMoveLine.getProduct();
      if (product == null) {
        return;
      }
      TrackingNumberConfiguration trackingNumberConfiguration =
          (TrackingNumberConfiguration)
              productCompanyService.get(
                  product, "trackingNumberConfiguration", stockMove.getCompany());

      this.assignOrGenerateTrackingNumber(
          stockMoveLine,
          stockMove,
          product,
          trackingNumberConfiguration,
          type == StockMoveRepository.TYPE_OUTGOING ? TYPE_SALES : TYPE_PURCHASES);
    }
  }

  @Override
  public void fillRealQuantities(StockMoveLine stockMoveLine, StockMove stockMove, BigDecimal qty) {
    if (stockMoveLine != null) {
      stockMoveLine.setRealQty(qty);
      stockMoveLine.setTotalNetMass(qty.multiply(stockMoveLine.getNetMass()));
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void splitIntoFulfilledMoveLineAndUnfulfilledOne(StockMoveLine stockMoveLine)
      throws AxelorException {
    checkSplitConditions(stockMoveLine);
    StockMoveLine newStockMoveLine = stockMoveLineRepository.copy(stockMoveLine, false);
    this.updateStockMoveLinesOfSplit(newStockMoveLine, stockMoveLine);
    stockMoveLineRepository.save(newStockMoveLine);
  }

  protected void checkSplitConditions(StockMoveLine stockMoveLine) throws AxelorException {
    StockMove stockMove = stockMoveLine.getStockMove();
    if (stockMove != null) {
      Integer statusSelect = stockMove.getStatusSelect();
      List<Integer> authorizedStatus = new ArrayList<>();
      authorizedStatus.add(StockMoveRepository.STATUS_DRAFT);
      authorizedStatus.add(StockMoveRepository.STATUS_PLANNED);
      if (!authorizedStatus.contains(statusSelect)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(StockExceptionMessage.SPLIT_MOVE_LINE_WRONG_STATUS));
      }

      if (stockMove.getTypeSelect().equals(StockMoveRepository.TYPE_INTERNAL)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(StockExceptionMessage.SPLIT_MOVE_LINE_WRONG_TYPE));
      }
    }

    BigDecimal realQty = stockMoveLine.getRealQty();
    BigDecimal qty = stockMoveLine.getQty();
    if (realQty != null && (realQty.signum() < 1 || realQty.compareTo(qty) >= 0)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.SPLIT_MOVE_LINE_WRONG_REAL_QTY));
    }
  }

  protected void updateStockMoveLinesOfSplit(
      StockMoveLine unfulfilledStockMoveLine, StockMoveLine fulfilledStockMoveLine) {
    BigDecimal realQty = fulfilledStockMoveLine.getRealQty();
    unfulfilledStockMoveLine.setQty(fulfilledStockMoveLine.getQty().subtract(realQty));
    fulfilledStockMoveLine.setQty(realQty);
    unfulfilledStockMoveLine.setTotalNetMass(BigDecimal.ZERO);
    unfulfilledStockMoveLine.setRealQty(BigDecimal.ZERO);
    unfulfilledStockMoveLine.setStockMove(fulfilledStockMoveLine.getStockMove());
    unfulfilledStockMoveLine.setConformitySelect(0);
  }
}
