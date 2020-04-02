/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseProductService;
import com.axelor.apps.purchase.service.SupplierCatalogService;
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
import java.util.Map;

@RequestScoped
public class StockMoveLineSupplychainServiceImpl extends StockMoveLineServiceImpl {

  protected AccountManagementService accountManagementService;

  protected PriceListService priceListService;

  private PurchaseProductService productService;

  private UnitConversionService unitConversionService;

  @Inject private SupplierCatalogService supplierCatalogService;

  @Inject
  public StockMoveLineSupplychainServiceImpl(
      TrackingNumberService trackingNumberService,
      AppBaseService appBaseService,
      StockMoveService stockMoveService,
      AccountManagementService accountManagementService,
      PriceListService priceListService,
      PurchaseProductService productService,
      UnitConversionService unitConversionService) {
    super(trackingNumberService, appBaseService, stockMoveService);
    this.accountManagementService = accountManagementService;
    this.priceListService = priceListService;
    this.productService = productService;
    this.unitConversionService = unitConversionService;
  }

  @Override
  public StockMoveLine compute(StockMoveLine stockMoveLine, StockMove stockMove)
      throws AxelorException {
    BigDecimal unitPriceUntaxed = BigDecimal.ZERO;
    BigDecimal unitPriceTaxed = BigDecimal.ZERO;
    TaxLine taxLine = null;
    BigDecimal discountAmount;
    if (stockMove == null
        || (stockMove.getSaleOrder() == null && stockMove.getPurchaseOrder() == null)) {
      return super.compute(stockMoveLine, stockMove);
    } else {
      if (stockMoveLine.getProduct() != null) {
        BigDecimal price;
        if (stockMove.getSaleOrder() != null) {
          taxLine =
              accountManagementService.getTaxLine(
                  appBaseService.getTodayDate(),
                  stockMoveLine.getProduct(),
                  stockMove.getCompany(),
                  stockMove.getSaleOrder().getClientPartner().getFiscalPosition(),
                  false);
          price = stockMoveLine.getProduct().getSalePrice();

          PriceList priceList = stockMove.getSaleOrder().getPriceList();
          if (priceList != null) {
            PriceListLine priceListLine =
                priceListService.getPriceListLine(
                    stockMoveLine.getProduct(), stockMoveLine.getRealQty(), priceList);
            Map<String, Object> discounts =
                priceListService.getDiscounts(priceList, priceListLine, price);
            if (discounts != null) {
              discountAmount = (BigDecimal) discounts.get("discountAmount");
              price =
                  priceListService.computeDiscount(
                      price, (int) discounts.get("discountTypeSelect"), discountAmount);
            }
          }
        } else {
          taxLine =
              accountManagementService.getTaxLine(
                  appBaseService.getTodayDate(),
                  stockMoveLine.getProduct(),
                  stockMove.getCompany(),
                  stockMove.getPurchaseOrder().getSupplierPartner().getFiscalPosition(),
                  true);
          price = stockMoveLine.getProduct().getPurchasePrice();

          BigDecimal catalogPrice =
              (BigDecimal)
                  supplierCatalogService
                      .updateInfoFromCatalog(
                          stockMoveLine.getProduct(),
                          stockMoveLine.getRealQty(),
                          stockMove.getPurchaseOrder().getSupplierPartner(),
                          stockMove.getPurchaseOrder().getCurrency(),
                          stockMove.getPurchaseOrder().getOrderDate())
                      .get("price");

          if (catalogPrice != null) {
            price = catalogPrice;
          }

          PriceList priceList = stockMove.getPurchaseOrder().getPriceList();
          if (priceList != null) {
            PriceListLine priceListLine =
                priceListService.getPriceListLine(
                    stockMoveLine.getProduct(), stockMoveLine.getRealQty(), priceList);
            Map<String, Object> discounts =
                priceListService.getDiscounts(priceList, priceListLine, price);
            if (discounts != null) {
              discountAmount = (BigDecimal) discounts.get("discountAmount");
              price =
                  priceListService.computeDiscount(
                      price, (int) discounts.get("discountTypeSelect"), discountAmount);
            }
          }
        }

        if (stockMoveLine.getProduct().getInAti()) {
          unitPriceTaxed = price;
          unitPriceUntaxed =
              price.divide(taxLine.getValue().add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
        } else {
          unitPriceUntaxed = price;
          unitPriceTaxed = price.add(price.multiply(taxLine.getValue()));
        }
      }
    }
    stockMoveLine.setUnitPriceUntaxed(unitPriceUntaxed);
    stockMoveLine.setUnitPriceTaxed(unitPriceTaxed);
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
        createStockMoveLine(
            product, productName, description, quantity, null, null, null, unit, null, null);

    generatedStockMoveLine.setSaleOrderLine(saleOrderLine);
    generatedStockMoveLine.setPurchaseOrderLine(purchaseOrderLine);
    return generatedStockMoveLine;
  }
}
