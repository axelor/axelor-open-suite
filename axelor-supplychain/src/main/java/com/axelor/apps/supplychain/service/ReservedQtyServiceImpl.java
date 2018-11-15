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
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public class ReservedQtyServiceImpl implements ReservedQtyService {

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
        Beans.get(StockLocationLineService.class).getStockLocationLine(stockLocation, product);
    if (stockLocationLine == null) {
      return;
    }
    // the quantity that will be allocated in stock location line
    BigDecimal realReservedQty;

    // the quantity that will be allocated in stock move line
    BigDecimal realReservedStockMoveQty;

    // if the status was planned, subtract the quantity using the previously allocated quantity.
    if (fromStatus == StockMoveRepository.STATUS_PLANNED) {
      realReservedStockMoveQty = stockMoveLine.getReservedQty().negate();

      // convert the quantity for stock location line

      realReservedQty =
          convertUnitWithProduct(
              stockMoveLineUnit, productUnit, realReservedStockMoveQty, stockMoveLine.getProduct());
    } else {
      realReservedQty = computeRealReservedQty(stockLocationLine, requestedReservedQty);
      // convert back the quantity for the stock move line
      realReservedStockMoveQty =
          convertUnitWithProduct(
              productUnit, stockMoveLineUnit, realReservedQty, stockMoveLine.getProduct());
      stockMoveLine.setReservedQty(realReservedStockMoveQty);
    }
    stockMoveLine.setReservedQty(realReservedStockMoveQty);
    stockLocationLine.setReservedQty(stockLocationLine.getReservedQty().add(realReservedQty));
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
    if (toStatus == StockMoveRepository.STATUS_REALIZED) {
      Unit stockMoveLineUnit = stockMoveLine.getUnit();
      Unit productUnit = product.getUnit();
      StockLocationLine stockLocationLine =
          Beans.get(StockLocationLineService.class).getStockLocationLine(stockLocation, product);
      if (stockLocationLine == null) {
        return;
      }
      // the quantity that will be allocated in stock location line
      BigDecimal realReservedQty;

      // the quantity that will be allocated in stock move line
      BigDecimal realReservedStockMoveQty;
      BigDecimal leftToAllocate =
          stockLocationLine.getRequestedReservedQty().subtract(stockLocationLine.getReservedQty());
      realReservedQty = qty.min(leftToAllocate);

      realReservedStockMoveQty =
          convertUnitWithProduct(productUnit, stockMoveLineUnit, realReservedQty, product);
      stockMoveLine.setReservedQty(realReservedStockMoveQty);
      stockLocationLine.setReservedQty(stockLocationLine.getReservedQty().add(realReservedQty));
      allocateReservedQuantityInSaleOrderLines(realReservedStockMoveQty, stockLocation, product);
    }
  }

  @Override
  public void allocateReservedQuantityInSaleOrderLines(
      BigDecimal qtyToAllocate, StockLocation stockLocation, Product product) {
    List<StockMoveLine> stockMoveLineListToAllocate =
        Beans.get(StockMoveLineRepository.class)
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

      // update stock move line
      stockMoveLine.setReservedQty(stockMoveLine.getReservedQty().add(allocatedQty));
      // update sale order line
      SaleOrderLine saleOrderLine = stockMoveLine.getSaleOrderLine();
      if (saleOrderLine != null) {
        saleOrderLine.setReservedQty(saleOrderLine.getReservedQty().add(allocatedQty));
      }
      // update left qty to allocate
      leftQtyToAllocate = leftQtyToAllocate.subtract(allocatedQty);
    }
  }

  @Override
  public BigDecimal computeRealReservedQty(
      StockLocationLine stockLocationLine, BigDecimal requestedReservedQty) {

    BigDecimal qtyLeftToBeAllocated =
        stockLocationLine.getCurrentQty().subtract(stockLocationLine.getReservedQty());
    return qtyLeftToBeAllocated.min(requestedReservedQty);
  }

  /** Convert but with null check. Return start value if one unit is null. */
  private BigDecimal convertUnitWithProduct(
      Unit startUnit, Unit endUnit, BigDecimal qtyToConvert, Product product)
      throws AxelorException {
    if (startUnit != null && !startUnit.equals(endUnit)) {
      return Beans.get(UnitConversionService.class)
          .convert(startUnit, endUnit, qtyToConvert, qtyToConvert.scale(), product);
    } else {
      return qtyToConvert;
    }
  }
}
