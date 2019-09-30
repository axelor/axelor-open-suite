/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveLineServiceImpl;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.TrackingNumberService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RequestScoped
public class StockMoveLineSupplychainServiceImpl extends StockMoveLineServiceImpl {

  protected AccountManagementService accountManagementService;

  protected PriceListService priceListService;

  private UnitConversionService unitConversionService;

  @Inject
  public StockMoveLineSupplychainServiceImpl(
      TrackingNumberService trackingNumberService,
      AppBaseService appBaseService,
      StockMoveService stockMoveService,
      AccountManagementService accountManagementService,
      PriceListService priceListService,
      UnitConversionService unitConversionService) {
    super(trackingNumberService, appBaseService, stockMoveService);
    this.accountManagementService = accountManagementService;
    this.priceListService = priceListService;
    this.unitConversionService = unitConversionService;
  }

  @Override
  public StockMoveLine compute(StockMoveLine stockMoveLine, StockMove stockMove)
      throws AxelorException {
    BigDecimal valuatedUnitPrice = BigDecimal.ZERO;

    if (stockMove == null
        || (stockMove.getSaleOrder() == null && stockMove.getPurchaseOrder() == null)) {
      return super.compute(stockMoveLine, stockMove);
    } else {
      if (stockMoveLine.getProduct() != null) {
        if (stockMove.getSaleOrder() != null) {
          // if stockmoveline is linked to a sale line, get its exTax unit price.
          if (stockMoveLine.getSaleOrderLine() != null) {
            if (stockMoveLine.getSaleOrderLine().getQty().compareTo(BigDecimal.ZERO) != 0) {
              valuatedUnitPrice =
                  stockMoveLine
                      .getSaleOrderLine()
                      .getCompanyExTaxTotal()
                      .divide(stockMoveLine.getSaleOrderLine().getQty());
            }
          } else {
            return super.compute(stockMoveLine, stockMove);
          }
        } else {
          // if stockmoveline is linked to a purchase line, get its exTax unit price.
          if (stockMoveLine.getPurchaseOrderLine() != null) {
            if (stockMoveLine.getPurchaseOrderLine().getQty().compareTo(BigDecimal.ZERO) != 0) {
              valuatedUnitPrice =
                  stockMoveLine
                      .getPurchaseOrderLine()
                      .getCompanyExTaxTotal()
                      .divide(stockMoveLine.getPurchaseOrderLine().getQty());
            }
          } else {
            return super.compute(stockMoveLine, stockMove);
          }
        }
      }
    }
    stockMoveLine.setValuatedUnitPrice(valuatedUnitPrice);
    return stockMoveLine;
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void updateReservedQty(StockMoveLine stockMoveLine, BigDecimal reservedQty)
      throws AxelorException {
    StockMove stockMove = stockMoveLine.getStockMove();
    int statusSelect = stockMove.getStatusSelect();
    if (statusSelect == StockMoveRepository.STATUS_PLANNED
        || statusSelect == StockMoveRepository.STATUS_REALIZED) {
      StockMoveService stockMoveService = Beans.get(StockMoveService.class);
      stockMoveService.cancel(stockMoveLine.getStockMove());
      stockMoveLine.setReservedQty(reservedQty);
      stockMoveService.goBackToDraft(stockMove);
      stockMoveService.plan(stockMove);
      if (statusSelect == StockMoveRepository.STATUS_REALIZED) {
        stockMoveService.realize(stockMove);
      }
    } else {
      stockMoveLine.setReservedQty(stockMoveLine.getReservedQty());
    }
  }

  @Override
  public void updateLocations(
      StockMoveLine stockMoveLine,
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      Product product,
      BigDecimal qty,
      int fromStatus,
      int toStatus,
      LocalDate lastFutureStockMoveDate,
      TrackingNumber trackingNumber,
      BigDecimal reservedQty)
      throws AxelorException {
    BigDecimal realReservedQty = stockMoveLine.getReservedQty();
    Unit productUnit = product.getUnit();
    Unit stockMoveLineUnit = stockMoveLine.getUnit();
    if (productUnit != null && !productUnit.equals(stockMoveLineUnit)) {
      qty =
          unitConversionService.convert(
              stockMoveLineUnit, productUnit, qty, qty.scale(), stockMoveLine.getProduct());
      realReservedQty =
          unitConversionService.convert(
              stockMoveLineUnit,
              productUnit,
              realReservedQty,
              realReservedQty.scale(),
              stockMoveLine.getProduct());
    }
    super.updateLocations(
        stockMoveLine,
        fromStockLocation,
        toStockLocation,
        product,
        qty,
        fromStatus,
        toStatus,
        lastFutureStockMoveDate,
        trackingNumber,
        realReservedQty);
  }

  public StockMoveLine getMergedStockMoveLine(List<StockMoveLine> stockMoveLineList)
      throws AxelorException {
    if (stockMoveLineList == null || stockMoveLineList.isEmpty()) {
      return null;
    }

    if (stockMoveLineList.size() == 1) {
      return stockMoveLineList.get(0);
    }

    SaleOrderLine saleOrderLine = stockMoveLineList.get(0).getSaleOrderLine();
    PurchaseOrderLine purchaseOrderLine = stockMoveLineList.get(0).getPurchaseOrderLine();

    Product product;
    String productName;
    String description;
    BigDecimal quantity = BigDecimal.ZERO;
    Unit unit;

    if (saleOrderLine != null) {
      product = saleOrderLine.getProduct();
      productName = saleOrderLine.getProductName();
      description = saleOrderLine.getDescription();
      unit = saleOrderLine.getUnit();

    } else if (purchaseOrderLine != null) {
      product = purchaseOrderLine.getProduct();
      productName = purchaseOrderLine.getProductName();
      description = purchaseOrderLine.getDescription();
      unit = purchaseOrderLine.getUnit();

    } else {
      return null; // shouldn't ever happen or you misused this function
    }

    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      quantity = quantity.add(stockMoveLine.getRealQty());
    }

    StockMoveLine generatedStockMoveLine =
        createStockMoveLine(product, productName, description, quantity, null, unit, null, null);

    generatedStockMoveLine.setSaleOrderLine(saleOrderLine);
    generatedStockMoveLine.setPurchaseOrderLine(purchaseOrderLine);
    return generatedStockMoveLine;
  }
}
