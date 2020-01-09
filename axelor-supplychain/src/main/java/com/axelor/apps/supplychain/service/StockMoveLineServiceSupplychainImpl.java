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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.stock.service.StockMoveLineServiceImpl;
import com.axelor.apps.stock.service.StockMoveToolService;
import com.axelor.apps.stock.service.TrackingNumberService;
import com.axelor.apps.stock.service.WeightedAveragePriceService;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.util.List;

@RequestScoped
public class StockMoveLineServiceSupplychainImpl extends StockMoveLineServiceImpl
    implements StockMoveLineServiceSupplychain {

  protected AccountManagementService accountManagementService;
  protected PriceListService priceListService;

  @Inject
  public StockMoveLineServiceSupplychainImpl(
      TrackingNumberService trackingNumberService,
      AppBaseService appBaseService,
      AppStockService appStockService,
      StockMoveToolService stockMoveToolService,
      StockMoveLineRepository stockMoveLineRepository,
      StockLocationLineService stockLocationLineService,
      UnitConversionService unitConversionService,
      WeightedAveragePriceService weightedAveragePriceService,
      TrackingNumberRepository trackingNumberRepo,
      AccountManagementService accountManagementService,
      PriceListService priceListService) {
    super(
        trackingNumberService,
        appBaseService,
        appStockService,
        stockMoveToolService,
        stockMoveLineRepository,
        stockLocationLineService,
        unitConversionService,
        weightedAveragePriceService,
        trackingNumberRepo);
    this.accountManagementService = accountManagementService;
    this.priceListService = priceListService;
  }

  @Override
  public StockMoveLine createStockMoveLine(
      Product product,
      String productName,
      String description,
      BigDecimal quantity,
      BigDecimal requestedReservedQty,
      BigDecimal unitPrice,
      BigDecimal companyUnitPriceUntaxed,
      Unit unit,
      StockMove stockMove,
      int type,
      boolean taxed,
      BigDecimal taxRate,
      SaleOrderLine saleOrderLine,
      PurchaseOrderLine purchaseOrderLine)
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
              unit,
              stockMove,
              taxed,
              taxRate);
      stockMoveLine.setRequestedReservedQty(requestedReservedQty);
      stockMoveLine.setSaleOrderLine(saleOrderLine);
      stockMoveLine.setPurchaseOrderLine(purchaseOrderLine);
      TrackingNumberConfiguration trackingNumberConfiguration =
          product.getTrackingNumberConfiguration();

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
          unit,
          stockMove,
          null);
    }
  }

  @Override
  public StockMoveLine compute(StockMoveLine stockMoveLine, StockMove stockMove)
      throws AxelorException {

    // the case when stockMove is null is made in super.
    if (stockMove == null) {
      return super.compute(stockMoveLine, null);
    }

    // if this is a pack do not compute price
    if (stockMoveLine.getProduct() == null
        || (stockMoveLine.getLineTypeSelect() != null
            && stockMoveLine.getLineTypeSelect() == StockMoveLineRepository.TYPE_PACK)) {
      return stockMoveLine;
    }

    if (stockMove.getOriginId() != null && stockMove.getOriginId() != 0L) {
      // the stock move comes from a sale or purchase order, we take the price from the order.
      stockMoveLine = computeFromOrder(stockMoveLine, stockMove);
    } else {
      stockMoveLine = super.compute(stockMoveLine, stockMove);
    }
    return stockMoveLine;
  }

  protected StockMoveLine computeFromOrder(StockMoveLine stockMoveLine, StockMove stockMove)
      throws AxelorException {
    BigDecimal unitPriceUntaxed = stockMoveLine.getUnitPriceUntaxed();
    BigDecimal unitPriceTaxed = stockMoveLine.getUnitPriceTaxed();
    Unit orderUnit = null;
    if (StockMoveRepository.ORIGIN_SALE_ORDER.equals(stockMove.getOriginTypeSelect())) {
      SaleOrderLine saleOrderLine = stockMoveLine.getSaleOrderLine();
      if (saleOrderLine == null) {
        // log the exception
        TraceBackService.trace(
            new AxelorException(
                TraceBackRepository.TYPE_TECHNICAL,
                IExceptionMessage.STOCK_MOVE_MISSING_SALE_ORDER,
                stockMove.getOriginId(),
                stockMove.getName()));
      } else {
        unitPriceUntaxed = saleOrderLine.getPriceDiscounted();
        unitPriceTaxed = saleOrderLine.getInTaxPrice();
        orderUnit = saleOrderLine.getUnit();
      }
    } else if (StockMoveRepository.ORIGIN_PURCHASE_ORDER.equals(stockMove.getOriginTypeSelect())) {
      PurchaseOrderLine purchaseOrderLine = stockMoveLine.getPurchaseOrderLine();
      if (purchaseOrderLine == null) {
        // log the exception
        TraceBackService.trace(
            new AxelorException(
                TraceBackRepository.TYPE_TECHNICAL,
                IExceptionMessage.STOCK_MOVE_MISSING_PURCHASE_ORDER,
                stockMove.getOriginId(),
                stockMove.getName()));
      } else {
        unitPriceUntaxed = purchaseOrderLine.getPrice();
        unitPriceTaxed = purchaseOrderLine.getInTaxPrice();
        orderUnit = purchaseOrderLine.getUnit();
      }
    }

    stockMoveLine.setUnitPriceUntaxed(unitPriceUntaxed);
    stockMoveLine.setUnitPriceTaxed(unitPriceTaxed);

    Unit stockUnit = getStockUnit(stockMoveLine);
    return convertUnitPrice(stockMoveLine, orderUnit, stockUnit);
  }

  protected StockMoveLine convertUnitPrice(StockMoveLine stockMoveLine, Unit fromUnit, Unit toUnit)
      throws AxelorException {
    // convert units
    if (toUnit != null && fromUnit != null) {
      BigDecimal unitPriceUntaxed =
          unitConversionService.convert(
              fromUnit,
              toUnit,
              stockMoveLine.getUnitPriceUntaxed(),
              appBaseService.getNbDecimalDigitForUnitPrice(),
              null);
      BigDecimal unitPriceTaxed =
          unitConversionService.convert(
              fromUnit,
              toUnit,
              stockMoveLine.getUnitPriceTaxed(),
              appBaseService.getNbDecimalDigitForUnitPrice(),
              null);
      stockMoveLine.setUnitPriceUntaxed(unitPriceUntaxed);
      stockMoveLine.setUnitPriceTaxed(unitPriceTaxed);
    }
    return stockMoveLine;
  }

  @Override
  public StockMoveLine splitStockMoveLine(
      StockMoveLine stockMoveLine, BigDecimal qty, TrackingNumber trackingNumber)
      throws AxelorException {

    StockMoveLine newStockMoveLine = super.splitStockMoveLine(stockMoveLine, qty, trackingNumber);

    BigDecimal reservedQtyAfterSplit =
        BigDecimal.ZERO.max(stockMoveLine.getRequestedReservedQty().subtract(qty));
    BigDecimal reservedQtyInNewLine = stockMoveLine.getRequestedReservedQty().min(qty);
    stockMoveLine.setRequestedReservedQty(reservedQtyAfterSplit);
    newStockMoveLine.setRequestedReservedQty(reservedQtyInNewLine);
    newStockMoveLine.setSaleOrderLine(stockMoveLine.getSaleOrderLine());
    newStockMoveLine.setPurchaseOrderLine(stockMoveLine.getPurchaseOrderLine());
    return newStockMoveLine;
  }

  @Override
  public void updateAvailableQty(StockMoveLine stockMoveLine, StockLocation stockLocation) {
    BigDecimal availableQty = BigDecimal.ZERO;
    BigDecimal availableQtyForProduct = BigDecimal.ZERO;

    if (stockMoveLine.getProduct() != null) {
      if (stockMoveLine.getProduct().getTrackingNumberConfiguration() != null) {

        if (stockMoveLine.getTrackingNumber() != null) {
          StockLocationLine stockLocationLine =
              stockLocationLineService.getDetailLocationLine(
                  stockLocation, stockMoveLine.getProduct(), stockMoveLine.getTrackingNumber());

          if (stockLocationLine != null) {
            availableQty =
                stockLocationLine
                    .getCurrentQty()
                    .add(stockMoveLine.getReservedQty())
                    .subtract(stockLocationLine.getReservedQty());
          }
        }

        if (availableQty.compareTo(stockMoveLine.getRealQty()) < 0) {
          StockLocationLine stockLocationLineForProduct =
              stockLocationLineService.getStockLocationLine(
                  stockLocation, stockMoveLine.getProduct());

          if (stockLocationLineForProduct != null) {
            availableQtyForProduct =
                stockLocationLineForProduct
                    .getCurrentQty()
                    .add(stockMoveLine.getReservedQty())
                    .subtract(stockLocationLineForProduct.getReservedQty());
          }
        }
      } else {
        StockLocationLine stockLocationLine =
            stockLocationLineService.getStockLocationLine(
                stockLocation, stockMoveLine.getProduct());

        if (stockLocationLine != null) {
          availableQty =
              stockLocationLine
                  .getCurrentQty()
                  .add(stockMoveLine.getReservedQty())
                  .subtract(stockLocationLine.getReservedQty());
        }
      }
    }
    stockMoveLine.setAvailableQty(availableQty);
    stockMoveLine.setAvailableQtyForProduct(availableQtyForProduct);
  }

  @Override
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
            product,
            productName,
            description,
            quantity,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            unit,
            null,
            null);

    generatedStockMoveLine.setSaleOrderLine(saleOrderLine);
    generatedStockMoveLine.setPurchaseOrderLine(purchaseOrderLine);
    return generatedStockMoveLine;
  }

  /**
   * This implementation copy fields from sale order line and purchase order line if the link
   * exists.
   *
   * @param stockMove
   * @param stockMoveLine
   * @param company
   */
  @Override
  public void setProductInfo(StockMove stockMove, StockMoveLine stockMoveLine, Company company)
      throws AxelorException {
    Preconditions.checkNotNull(stockMoveLine);
    Preconditions.checkNotNull(company);

    Product product = stockMoveLine.getProduct();

    if (product == null) {
      return;
    }
    super.setProductInfo(stockMove, stockMoveLine, company);

    SaleOrderLine saleOrderLine = stockMoveLine.getSaleOrderLine();
    PurchaseOrderLine purchaseOrderLine = stockMoveLine.getPurchaseOrderLine();

    if (saleOrderLine != null) {
      setProductInfoFromSaleOrder(stockMoveLine, saleOrderLine);
    }
    if (purchaseOrderLine != null) {
      setProductInfoFromPurchaseOrder(stockMoveLine, purchaseOrderLine);
    }
  }

  protected void setProductInfoFromSaleOrder(
      StockMoveLine stockMoveLine, SaleOrderLine saleOrderLine) {

    stockMoveLine.setUnit(saleOrderLine.getUnit());
    stockMoveLine.setProductName(saleOrderLine.getProductName());
    if (Strings.isNullOrEmpty(stockMoveLine.getDescription())) {
      stockMoveLine.setDescription(saleOrderLine.getDescription());
    }
    stockMoveLine.setQty(saleOrderLine.getQty());
  }

  protected void setProductInfoFromPurchaseOrder(
      StockMoveLine stockMoveLine, PurchaseOrderLine purchaseOrderLine) {

    stockMoveLine.setUnit(purchaseOrderLine.getUnit());
    stockMoveLine.setProductName(purchaseOrderLine.getProductName());
    if (Strings.isNullOrEmpty(stockMoveLine.getDescription())) {
      stockMoveLine.setDescription(purchaseOrderLine.getDescription());
    }
    stockMoveLine.setQty(purchaseOrderLine.getQty());
  }

  @Override
  public boolean isAvailableProduct(StockMove stockMove, StockMoveLine stockMoveLine) {
    if (stockMoveLine.getProduct() == null
        || (stockMoveLine.getProduct() != null && !stockMoveLine.getProduct().getStockManaged())) {
      return true;
    }
    updateAvailableQty(stockMoveLine, stockMove.getFromStockLocation());
    BigDecimal availableQty = stockMoveLine.getAvailableQty();
    if (stockMoveLine.getProduct().getTrackingNumberConfiguration() != null
        && stockMoveLine.getTrackingNumber() == null) {
      availableQty = stockMoveLine.getAvailableQtyForProduct();
    }
    BigDecimal realQty = stockMoveLine.getRealQty();
    if (availableQty.compareTo(realQty) < 0) {
      return false;
    }
    return true;
  }

  @Override
  public void setInvoiceStatus(StockMoveLine stockMoveLine) {
    if (stockMoveLine.getQtyInvoiced().compareTo(BigDecimal.ZERO) == 0) {
      stockMoveLine.setAvailableStatus(I18n.get("Not invoiced"));
      stockMoveLine.setAvailableStatusSelect(3);
    } else if (stockMoveLine.getQtyInvoiced().compareTo(stockMoveLine.getRealQty()) == -1) {
      stockMoveLine.setAvailableStatus(I18n.get("Partially invoiced"));
      stockMoveLine.setAvailableStatusSelect(4);
    } else if (stockMoveLine.getQtyInvoiced().compareTo(stockMoveLine.getRealQty()) == 0) {
      stockMoveLine.setAvailableStatus(I18n.get("Invoiced"));
      stockMoveLine.setAvailableStatusSelect(1);
    }
  }
}
