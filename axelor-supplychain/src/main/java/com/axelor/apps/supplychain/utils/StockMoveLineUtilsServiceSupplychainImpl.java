/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.utils;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.stock.utils.StockMoveLineUtilsServiceImpl;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class StockMoveLineUtilsServiceSupplychainImpl extends StockMoveLineUtilsServiceImpl
    implements StockMoveLineUtilsServiceSupplychain {

  protected AppBaseService appBaseService;
  protected UnitConversionService unitConversionService;

  @Inject
  public StockMoveLineUtilsServiceSupplychainImpl(
      ProductCompanyService productCompanyService,
      AppBaseService appBaseService,
      UnitConversionService unitConversionService) {
    super(productCompanyService);
    this.appBaseService = appBaseService;
    this.unitConversionService = unitConversionService;
  }

  @Override
  public void updateAvailableQty(StockMoveLine stockMoveLine, StockLocation stockLocation)
      throws AxelorException {

    if (!appBaseService.isApp("supplychain")) {
      super.updateAvailableQty(stockMoveLine, stockLocation);
      return;
    }

    BigDecimal availableQty = BigDecimal.ZERO;
    BigDecimal availableQtyForProduct = BigDecimal.ZERO;

    TrackingNumberConfiguration trackingNumberConfiguration;

    if (stockMoveLine.getProduct() != null) {
      trackingNumberConfiguration =
          (TrackingNumberConfiguration)
              productCompanyService.get(
                  stockMoveLine.getProduct(),
                  "trackingNumberConfiguration",
                  stockLocation.getCompany());
    } else {
      trackingNumberConfiguration = null;
    }

    StockLocationLineService stockLocationLineService = Beans.get(StockLocationLineService.class);

    if (stockMoveLine.getProduct() != null) {
      if (trackingNumberConfiguration != null) {

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
  public void setInvoiceStatus(StockMoveLine stockMoveLine) {
    if (stockMoveLine.getQtyInvoiced().compareTo(BigDecimal.ZERO) == 0) {
      stockMoveLine.setAvailableStatus(I18n.get("Not invoiced"));
      stockMoveLine.setAvailableStatusSelect(3);
    } else if (stockMoveLine.getQtyInvoiced().compareTo(stockMoveLine.getRealQty()) < 0) {
      stockMoveLine.setAvailableStatus(I18n.get("Partially invoiced"));
      stockMoveLine.setAvailableStatusSelect(4);
    } else if (stockMoveLine.getQtyInvoiced().compareTo(stockMoveLine.getRealQty()) == 0) {
      stockMoveLine.setAvailableStatus(I18n.get("Invoiced"));
      stockMoveLine.setAvailableStatusSelect(1);
    }
  }

  @Override
  public boolean isAllocatedStockMoveLine(StockMoveLine stockMoveLine) {
    return stockMoveLine.getReservedQty().compareTo(BigDecimal.ZERO) > 0
        || stockMoveLine.getRequestedReservedQty().compareTo(BigDecimal.ZERO) > 0;
  }

  @Override
  public BigDecimal getAmountNotInvoiced(
      StockMoveLine stockMoveLine, boolean isPurchase, boolean ati, boolean recoveredTax)
      throws AxelorException {
    return this.getAmountNotInvoiced(
        stockMoveLine,
        stockMoveLine.getPurchaseOrderLine(),
        stockMoveLine.getSaleOrderLine(),
        isPurchase,
        ati,
        recoveredTax);
  }

  @Override
  public BigDecimal getAmountNotInvoiced(
      StockMoveLine stockMoveLine,
      PurchaseOrderLine purchaseOrderLine,
      SaleOrderLine saleOrderLine,
      boolean isPurchase,
      boolean ati,
      boolean recoveredTax)
      throws AxelorException {
    BigDecimal amountInCurrency = null;
    BigDecimal totalQty = null;
    BigDecimal notInvoicedQty = null;

    if (isPurchase && purchaseOrderLine != null) {
      totalQty = purchaseOrderLine.getQty();

      notInvoicedQty =
          unitConversionService.convert(
              stockMoveLine.getUnit(),
              purchaseOrderLine.getUnit(),
              stockMoveLine.getRealQty().subtract(stockMoveLine.getQtyInvoiced()),
              stockMoveLine.getRealQty().scale(),
              purchaseOrderLine.getProduct());

      if (ati && !recoveredTax) {
        amountInCurrency = purchaseOrderLine.getInTaxTotal();
      } else {
        amountInCurrency = purchaseOrderLine.getExTaxTotal();
      }
    } else if (!isPurchase && saleOrderLine != null) {
      totalQty = saleOrderLine.getQty();

      notInvoicedQty =
          unitConversionService.convert(
              stockMoveLine.getUnit(),
              saleOrderLine.getUnit(),
              stockMoveLine.getRealQty().subtract(stockMoveLine.getQtyInvoiced()),
              stockMoveLine.getRealQty().scale(),
              saleOrderLine.getProduct());
      if (ati) {
        amountInCurrency = saleOrderLine.getInTaxTotal();
      } else {
        amountInCurrency = saleOrderLine.getExTaxTotal();
      }
    }

    if (totalQty == null || BigDecimal.ZERO.compareTo(totalQty) == 0) {
      return null;
    }

    BigDecimal qtyRate = notInvoicedQty.divide(totalQty, 10, RoundingMode.HALF_UP);
    return amountInCurrency.multiply(qtyRate).setScale(2, RoundingMode.HALF_UP);
  }
}
