/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public class ReservedQtyServiceImpl implements ReservedQtyService {

  protected StockLocationLineService stockLocationLineService;
  protected StockMoveLineRepository stockMoveLineRepository;
  protected UnitConversionService unitConversionService;

  @Inject
  public ReservedQtyServiceImpl(
      StockLocationLineService stockLocationLineService,
      StockMoveLineRepository stockMoveLineRepository,
      UnitConversionService unitConversionService) {
    this.stockLocationLineService = stockLocationLineService;
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.unitConversionService = unitConversionService;
  }

  @Override
  public void updateRequestedQuantityInLocations(
      StockMoveLine stockMoveLine,
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      Product product,
      BigDecimal qty,
      BigDecimal convertedRequestedReservedQty,
      int fromStatus,
      int toStatus)
      throws AxelorException {
    if (fromStockLocation.getTypeSelect() != StockLocationRepository.TYPE_VIRTUAL) {
      updateRequestedQuantityInFromStockLocation(
          stockMoveLine,
          fromStockLocation,
          product,
          fromStatus,
          toStatus,
          convertedRequestedReservedQty);
    }
    if (toStockLocation.getTypeSelect() != StockLocationRepository.TYPE_VIRTUAL) {
      updateRequestedQuantityInToStockLocation(
          stockMoveLine, toStockLocation, product, fromStatus, toStatus, qty);
    }
  }

  @Override
  public void updateRequestedQuantityInFromStockLocation(
      StockMoveLine stockMoveLine,
      StockLocation stockLocation,
      Product product,
      int fromStatus,
      int toStatus,
      BigDecimal requestedReservedQty)
      throws AxelorException {
    Unit stockMoveLineUnit = stockMoveLine.getUnit();
    Unit productUnit = product.getUnit();

    StockLocationLine stockLocationLine =
        stockLocationLineService.getStockLocationLine(stockLocation, product);
    if (stockLocationLine == null) {
      return;
    }
    // the quantity that will be allocated in stock location line
    BigDecimal realReservedQty;

    // the quantity that will be allocated in stock move line
    BigDecimal realReservedStockMoveQty;

    // if the status was planned, subtract the quantity using the previously allocated quantity.
    if (toStatus == StockMoveRepository.STATUS_CANCELED
        || toStatus == StockMoveRepository.STATUS_REALIZED) {
      realReservedStockMoveQty = stockMoveLine.getReservedQty();

      // convert the quantity for stock location line

      realReservedQty =
          convertUnitWithProduct(
              stockMoveLineUnit, productUnit, realReservedStockMoveQty, stockMoveLine.getProduct());

      // update allocated quantity in stock location line
      stockLocationLine.setReservedQty(
          stockLocationLine.getReservedQty().subtract(realReservedQty));

      // reallocate quantity in other stock move lines
      // reallocateQty(stockMoveLine, stockLocation, stockLocationLine, product, realReservedQty);

      // no more reserved qty in stock move and sale order lines
      updateReservedQuantityFromStockMoveLine(
          stockMoveLine, product, stockMoveLine.getReservedQty().negate());
    } else {
      realReservedQty = computeRealReservedQty(stockLocationLine, requestedReservedQty);
      // convert back the quantity for the stock move line
      realReservedStockMoveQty =
          convertUnitWithProduct(
              productUnit, stockMoveLineUnit, realReservedQty, stockMoveLine.getProduct());
      updateReservedQuantityFromStockMoveLine(stockMoveLine, product, realReservedStockMoveQty);
      stockLocationLine.setReservedQty(stockLocationLine.getReservedQty().add(realReservedQty));
    }

    checkReservedQtyStocks(stockLocationLine, fromStatus, toStatus);
  }

  @Override
  public void updateRequestedQuantityInToStockLocation(
      StockMoveLine stockMoveLine,
      StockLocation stockLocation,
      Product product,
      int fromStatus,
      int toStatus,
      BigDecimal qty)
      throws AxelorException {
    StockLocationLine stockLocationLine =
        stockLocationLineService.getStockLocationLine(stockLocation, product);
    if (stockLocationLine == null) {
      return;
    }
    if (toStatus == StockMoveRepository.STATUS_REALIZED) {
      reallocateQty(stockMoveLine, stockLocation, stockLocationLine, product, qty);
    }
    checkReservedQtyStocks(stockLocationLine, fromStatus, toStatus);
  }

  protected void reallocateQty(
      StockMoveLine stockMoveLine,
      StockLocation stockLocation,
      StockLocationLine stockLocationLine,
      Product product,
      BigDecimal qty)
      throws AxelorException {

    Unit stockMoveLineUnit = stockMoveLine.getUnit();
    Unit productUnit = product.getUnit();
    // the quantity that will be allocated in stock location line
    BigDecimal realReservedQty;

    // the quantity that will be allocated in stock move line
    BigDecimal realReservedStockMoveQty;
    BigDecimal leftToAllocate =
        stockLocationLine.getRequestedReservedQty().subtract(stockLocationLine.getReservedQty());
    realReservedQty = qty.min(leftToAllocate);

    realReservedStockMoveQty =
        convertUnitWithProduct(productUnit, stockMoveLineUnit, realReservedQty, product);
    stockLocationLine.setReservedQty(stockLocationLine.getReservedQty().add(realReservedQty));
    allocateReservedQuantityInSaleOrderLines(realReservedStockMoveQty, stockLocation, product);
  }

  @Override
  public void allocateReservedQuantityInSaleOrderLines(
      BigDecimal qtyToAllocate, StockLocation stockLocation, Product product)
      throws AxelorException {
    List<StockMoveLine> stockMoveLineListToAllocate =
        stockMoveLineRepository
            .all()
            .filter(
                "self.stockMove.fromStockLocation.id = :stockLocationId "
                    + "AND self.product.id = :productId "
                    + "AND self.stockMove.statusSelect = :planned "
                    + "AND self.stockMove.reservationDateTime IS NOT NULL "
                    + "AND self.reservedQty < self.requestedReservedQty")
            .bind("stockLocationId", stockLocation.getId())
            .bind("productId", product.getId())
            .bind("planned", StockMoveRepository.STATUS_PLANNED)
            .fetch();
    stockMoveLineListToAllocate.sort(
        Comparator.comparing(
            stockMoveLine -> stockMoveLine.getStockMove().getReservationDateTime()));
    BigDecimal leftQtyToAllocate = qtyToAllocate;
    for (StockMoveLine stockMoveLine : stockMoveLineListToAllocate) {
      BigDecimal neededQtyToAllocate =
          stockMoveLine.getRequestedReservedQty().subtract(stockMoveLine.getReservedQty());
      BigDecimal allocatedQty = leftQtyToAllocate.min(neededQtyToAllocate);

      BigDecimal allocatedStockMoveQty =
          convertUnitWithProduct(product.getUnit(), stockMoveLine.getUnit(), allocatedQty, product);

      // update reserved qty in stock move line and sale order line
      updateReservedQuantityFromStockMoveLine(stockMoveLine, product, allocatedStockMoveQty);
      // update left qty to allocate
      leftQtyToAllocate = leftQtyToAllocate.subtract(allocatedQty);
    }
  }

  @Override
  public void updateReservedQuantityFromStockMoveLine(
      StockMoveLine stockMoveLine, Product product, BigDecimal reservedQtyToAdd)
      throws AxelorException {
    SaleOrderLine saleOrderLine = stockMoveLine.getSaleOrderLine();
    stockMoveLine.setReservedQty(stockMoveLine.getReservedQty().add(reservedQtyToAdd));
    if (saleOrderLine != null) {
      BigDecimal soLineReservedQtyToAdd =
          convertUnitWithProduct(
              stockMoveLine.getUnit(), saleOrderLine.getUnit(), reservedQtyToAdd, product);
      saleOrderLine.setReservedQty(saleOrderLine.getReservedQty().add(soLineReservedQtyToAdd));
    }
  }

  @Override
  public void updateReservedQuantityInStockMoveLineFromSaleOrderLine(
      SaleOrderLine saleOrderLine, Product product, BigDecimal newReservedQty)
      throws AxelorException {
    BigDecimal saleOrderReservedQty =
        convertUnitWithProduct(
            saleOrderLine.getUnit(), saleOrderLine.getUnit(), newReservedQty, product);
    saleOrderLine.setReservedQty(saleOrderReservedQty);

    List<StockMoveLine> stockMoveLineList =
        stockMoveLineRepository
            .all()
            .filter(
                "self.saleOrderLine.id = :saleOrderLineId "
                    + "AND self.stockMove.statusSelect = :planned")
            .bind("saleOrderLineId", saleOrderLine.getId())
            .bind("planned", StockMoveRepository.STATUS_PLANNED)
            .fetch();
    BigDecimal allocatedQty = saleOrderLine.getReservedQty();
    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      BigDecimal reservedQtyInStockMoveLine =
          stockMoveLine.getRequestedReservedQty().min(allocatedQty);
      stockMoveLine.setReservedQty(reservedQtyInStockMoveLine);
      allocatedQty = allocatedQty.subtract(reservedQtyInStockMoveLine);
    }
  }

  /**
   * Allocated qty cannot be greater than available qty.
   *
   * @param stockLocationLine
   * @throws AxelorException
   */
  protected void checkReservedQtyStocks(
      StockLocationLine stockLocationLine, int fromStatus, int toStatus) throws AxelorException {

    if (((toStatus == StockMoveRepository.STATUS_REALIZED)
            || (fromStatus == StockMoveRepository.STATUS_REALIZED
                && toStatus == StockMoveRepository.STATUS_CANCELED))
        && stockLocationLine.getReservedQty().compareTo(stockLocationLine.getCurrentQty()) > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.LOCATION_LINE_NOT_ENOUGH_AVAILABLE_QTY));
    }
  }

  @Override
  public BigDecimal computeRealReservedQty(
      StockLocationLine stockLocationLine, BigDecimal requestedReservedQty) {

    BigDecimal qtyLeftToBeAllocated =
        stockLocationLine.getCurrentQty().subtract(stockLocationLine.getReservedQty());
    return qtyLeftToBeAllocated.min(requestedReservedQty);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void updateReservedQty(SaleOrderLine saleOrderLine, BigDecimal newReservedQty)
      throws AxelorException {
    StockMoveLine stockMoveLine = getPlannedStockMoveLine(saleOrderLine);

    if (stockMoveLine == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.SALE_ORDER_LINE_NO_STOCK_MOVE));
    }
    StockLocationLine stockLocationLine =
        stockLocationLineService.getStockLocationLine(
            stockMoveLine.getStockMove().getFromStockLocation(), stockMoveLine.getProduct());
    BigDecimal availableQtyToBeReserved =
        stockLocationLine.getCurrentQty().subtract(stockLocationLine.getReservedQty());
    BigDecimal diffReservedQuantity = newReservedQty.subtract(saleOrderLine.getReservedQty());
    if (availableQtyToBeReserved.compareTo(diffReservedQuantity) < 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.SALE_ORDER_LINE_QTY_NOT_AVAILABLE));
    }
    // update in stock move line and sale order line
    updateReservedQuantityInStockMoveLineFromSaleOrderLine(
        saleOrderLine, stockMoveLine.getProduct(), newReservedQty);

    Product product = stockMoveLine.getProduct();
    // update in stock location line
    BigDecimal diffReservedQuantityLocation =
        convertUnitWithProduct(
            stockMoveLine.getUnit(), product.getUnit(), diffReservedQuantity, product);
    stockLocationLine.setReservedQty(
        stockLocationLine.getReservedQty().add(diffReservedQuantityLocation));

    // update requested reserved qty
    if (newReservedQty.compareTo(saleOrderLine.getRequestedReservedQty()) > 0) {
      updateRequestedReservedQty(saleOrderLine, newReservedQty);
    }
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void updateRequestedReservedQty(SaleOrderLine saleOrderLine, BigDecimal newReservedQty)
      throws AxelorException {
    saleOrderLine.setRequestedReservedQty(newReservedQty);

    StockMoveLine stockMoveLine = getPlannedStockMoveLine(saleOrderLine);

    if (stockMoveLine == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.SALE_ORDER_LINE_NO_STOCK_MOVE));
    }
    // update in stock move line and sale order line
    stockMoveLine.setRequestedReservedQty(newReservedQty);
    saleOrderLine.setRequestedReservedQty(newReservedQty);

    StockLocationLine stockLocationLine =
        stockLocationLineService.getStockLocationLine(
            stockMoveLine.getStockMove().getFromStockLocation(), stockMoveLine.getProduct());

    BigDecimal diffReservedQuantity =
        newReservedQty.subtract(saleOrderLine.getRequestedReservedQty());
    Product product = stockMoveLine.getProduct();
    // update in stock location line
    BigDecimal diffReservedQuantityLocation =
        convertUnitWithProduct(
            stockMoveLine.getUnit(), product.getUnit(), diffReservedQuantity, product);
    stockLocationLine.setRequestedReservedQty(
        stockLocationLine.getRequestedReservedQty().add(diffReservedQuantityLocation));

    // update requested reserved qty
    if (newReservedQty.compareTo(saleOrderLine.getReservedQty()) < 0) {
      updateReservedQty(saleOrderLine, newReservedQty);
    }
  }

  protected StockMoveLine getPlannedStockMoveLine(SaleOrderLine saleOrderLine) {
    return stockMoveLineRepository
        .all()
        .filter(
            "self.saleOrderLine = :saleOrderLine " + "AND self.stockMove.statusSelect = :planned")
        .bind("saleOrderLine", saleOrderLine)
        .bind("planned", StockMoveRepository.STATUS_PLANNED)
        .fetchOne();
  }

  /** Convert but with null check. Return start value if one unit is null. */
  private BigDecimal convertUnitWithProduct(
      Unit startUnit, Unit endUnit, BigDecimal qtyToConvert, Product product)
      throws AxelorException {
    if (startUnit != null && !startUnit.equals(endUnit)) {
      return unitConversionService.convert(startUnit, endUnit, qtyToConvert, qtyToConvert.scale(), product);
    } else {
      return qtyToConvert;
    }
  }
}
