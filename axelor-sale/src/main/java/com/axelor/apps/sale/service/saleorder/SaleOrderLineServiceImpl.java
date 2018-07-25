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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.sale.db.PackLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderLineServiceImpl implements SaleOrderLineService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject protected CurrencyService currencyService;

  @Inject protected PriceListService priceListService;

  @Inject protected AppBaseService appBaseService;

  @Inject protected ProductMultipleQtyService productMultipleQtyService;

  @Override
  public void computeProductInformation(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    computeProductInformation(saleOrderLine, saleOrder, false);
  }

  @Override
  public void computeProductInformation(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, boolean taxLineIsOptional)
      throws AxelorException {
    AccountManagementService accountManagementService = Beans.get(AccountManagementService.class);

    Product product = saleOrderLine.getProduct();
    TaxLine taxLine;

    try {
      taxLine = getTaxLine(saleOrder, saleOrderLine);
      saleOrderLine.setTaxLine(taxLine);

      Tax tax =
          accountManagementService.getProductTax(
              accountManagementService.getAccountManagement(product, saleOrder.getCompany()),
              false);
      TaxEquiv taxEquiv =
          Beans.get(FiscalPositionService.class)
              .getTaxEquiv(saleOrder.getClientPartner().getFiscalPosition(), tax);

      saleOrderLine.setTaxEquiv(taxEquiv);
    } catch (AxelorException e) {
      if (!taxLineIsOptional) {
        throw e;
      }
      saleOrderLine.setTaxLine(null);
      saleOrderLine.setTaxEquiv(null);
      taxLine = null;
    }

    BigDecimal price = this.getUnitPrice(saleOrder, saleOrderLine, taxLine);

    saleOrderLine.setProductName(product.getName());
    saleOrderLine.setUnit(this.getSaleUnit(saleOrderLine));
    saleOrderLine.setCompanyCostPrice(this.getCompanyCostPrice(saleOrder, saleOrderLine));

    Map<String, Object> discounts = this.getDiscount(saleOrder, saleOrderLine, price);

    if (discounts != null) {
      saleOrderLine.setDiscountAmount(new BigDecimal(discounts.get("discountAmount").toString()));
      saleOrderLine.setDiscountTypeSelect((Integer) discounts.get("discountTypeSelect"));
      if (discounts.get("price") != null) {
        price = (BigDecimal) discounts.get("price");
      }
    }
    saleOrderLine.setPrice(price);
  }

  @Override
  public Map<String, BigDecimal> computeValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {

    HashMap<String, BigDecimal> map = new HashMap<>();
    if (saleOrder == null
        || (saleOrderLine.getProduct() == null && saleOrderLine.getProductName() == null)
        || saleOrderLine.getPrice() == null
        || saleOrderLine.getQty() == null) {
      return map;
    }

    BigDecimal exTaxTotal;
    BigDecimal companyExTaxTotal;
    BigDecimal inTaxTotal;
    BigDecimal companyInTaxTotal;
    BigDecimal priceDiscounted = this.computeDiscount(saleOrderLine);
    BigDecimal taxRate = BigDecimal.ZERO;

    if (saleOrderLine.getTaxLine() != null) {
      taxRate = saleOrderLine.getTaxLine().getValue();
    }

    if (!saleOrder.getInAti()) {
      exTaxTotal = this.computeAmount(saleOrderLine.getQty(), priceDiscounted);
      inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxRate));
      companyExTaxTotal = this.getAmountInCompanyCurrency(exTaxTotal, saleOrder);
      companyInTaxTotal = companyExTaxTotal.add(companyExTaxTotal.multiply(taxRate));
    } else {
      inTaxTotal = this.computeAmount(saleOrderLine.getQty(), priceDiscounted);
      exTaxTotal = inTaxTotal.divide(taxRate.add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
      companyInTaxTotal = this.getAmountInCompanyCurrency(inTaxTotal, saleOrder);
      companyExTaxTotal =
          companyInTaxTotal.divide(taxRate.add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
    }

    saleOrderLine.setInTaxTotal(inTaxTotal);
    saleOrderLine.setExTaxTotal(exTaxTotal);
    saleOrderLine.setPriceDiscounted(priceDiscounted);
    saleOrderLine.setCompanyInTaxTotal(companyInTaxTotal);
    saleOrderLine.setCompanyExTaxTotal(companyExTaxTotal);
    map.put("inTaxTotal", inTaxTotal);
    map.put("exTaxTotal", exTaxTotal);
    map.put("priceDiscounted", priceDiscounted);
    map.put("companyExTaxTotal", companyExTaxTotal);
    map.put("companyInTaxTotal", companyInTaxTotal);

    map.putAll(this.computeSubMargin(saleOrder, saleOrderLine));

    return map;
  }

  /**
   * Compute the excluded tax total amount of a sale order line.
   *
   * @param quantity The quantity.
   * @param price The unit price.
   * @return The excluded tax total amount.
   */
  @Override
  public BigDecimal computeAmount(SaleOrderLine saleOrderLine) {

    BigDecimal price = this.computeDiscount(saleOrderLine);

    BigDecimal amount = computeAmount(saleOrderLine.getQty(), price);

    return amount;
  }

  @Override
  public BigDecimal computeAmount(BigDecimal quantity, BigDecimal price) {

    BigDecimal amount =
        quantity
            .multiply(price)
            .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_EVEN);

    logger.debug(
        "Calcul du montant HT avec une quantité de {} pour {} : {}",
        new Object[] {quantity, price, amount});

    return amount;
  }

  @Override
  public BigDecimal getUnitPrice(SaleOrder saleOrder, SaleOrderLine saleOrderLine, TaxLine taxLine)
      throws AxelorException {

    Product product = saleOrderLine.getProduct();

    BigDecimal price = this.convertUnitPrice(product, taxLine, product.getSalePrice(), saleOrder);

    return currencyService
        .getAmountCurrencyConvertedAtDate(
            product.getSaleCurrency(), saleOrder.getCurrency(), price, saleOrder.getCreationDate())
        .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
  }

  @Override
  public TaxLine getTaxLine(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {

    return Beans.get(AccountManagementService.class)
        .getTaxLine(
            saleOrder.getCreationDate(),
            saleOrderLine.getProduct(),
            saleOrder.getCompany(),
            saleOrder.getClientPartner().getFiscalPosition(),
            false);
  }

  @Override
  public BigDecimal getAmountInCompanyCurrency(BigDecimal exTaxTotal, SaleOrder saleOrder)
      throws AxelorException {

    return currencyService
        .getAmountCurrencyConvertedAtDate(
            saleOrder.getCurrency(),
            saleOrder.getCompany().getCurrency(),
            exTaxTotal,
            saleOrder.getCreationDate())
        .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal getCompanyCostPrice(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {

    Product product = saleOrderLine.getProduct();

    return currencyService
        .getAmountCurrencyConvertedAtDate(
            product.getPurchaseCurrency(),
            saleOrder.getCompany().getCurrency(),
            product.getCostPrice(),
            saleOrder.getCreationDate())
        .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
  }

  @Override
  public PriceListLine getPriceListLine(SaleOrderLine saleOrderLine, PriceList priceList) {

    return priceListService.getPriceListLine(
        saleOrderLine.getProduct(), saleOrderLine.getQty(), priceList);
  }

  @Override
  public BigDecimal computeDiscount(SaleOrderLine saleOrderLine) {

    return priceListService.computeDiscount(
        saleOrderLine.getPrice(),
        saleOrderLine.getDiscountTypeSelect(),
        saleOrderLine.getDiscountAmount());
  }

  @Override
  public BigDecimal convertUnitPrice(
      Product product, TaxLine taxLine, BigDecimal price, SaleOrder saleOrder) {

    if (taxLine == null) {
      return price;
    }

    if (product.getInAti() && !saleOrder.getInAti()) {
      price = price.divide(taxLine.getValue().add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
    } else if (!product.getInAti() && saleOrder.getInAti()) {
      price = price.add(price.multiply(taxLine.getValue()));
    }
    return price;
  }

  @Override
  public Map<String, Object> getDiscount(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, BigDecimal price) {

    PriceList priceList = saleOrder.getPriceList();
    if (priceList == null) {
      return null;
    }

    PriceListLine priceListLine = this.getPriceListLine(saleOrderLine, priceList);
    return priceListService.getReplacedPriceAndDiscounts(priceList, priceListLine, price);
  }

  @Override
  public int getDiscountTypeSelect(SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    PriceList priceList = saleOrder.getPriceList();
    if (priceList != null) {
      PriceListLine priceListLine = this.getPriceListLine(saleOrderLine, priceList);

      return priceListLine.getTypeSelect();
    }
    return 0;
  }

  @Override
  public Unit getSaleUnit(SaleOrderLine saleOrderLine) {
    Unit unit = saleOrderLine.getProduct().getSalesUnit();
    if (unit == null) {
      unit = saleOrderLine.getProduct().getUnit();
    }
    return unit;
  }

  @Override
  public boolean unitPriceShouldBeUpdate(SaleOrder saleOrder, Product product) {

    if (product != null && product.getInAti() != saleOrder.getInAti()) {
      return true;
    }
    return false;
  }

  @Override
  public BigDecimal computeTotalPack(SaleOrderLine saleOrderLine) {

    BigDecimal totalPack = BigDecimal.ZERO;

    if (saleOrderLine.getSubLineList() != null) {
      for (SaleOrderLine subLine : saleOrderLine.getSubLineList()) {
        totalPack = totalPack.add(subLine.getInTaxTotal());
      }
    }

    return totalPack;
  }

  @Override
  public SaleOrder getSaleOrder(Context context) {

    Context parentContext = context.getParent();

    if (!parentContext.getContextClass().toString().equals(SaleOrder.class.toString())) {

      parentContext = parentContext.getParent();
    }

    if (parentContext == null) {
      return null;
    }

    SaleOrder saleOrder = parentContext.asType(SaleOrder.class);

    if (!parentContext.getContextClass().toString().equals(SaleOrder.class.toString())) {

      SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);

      saleOrder = saleOrderLine.getSaleOrder();
    }

    return saleOrder;
  }

  @Override
  public Map<String, BigDecimal> computeSubMargin(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {

    HashMap<String, BigDecimal> map = new HashMap<>();

    BigDecimal subTotalCostPrice = BigDecimal.ZERO;
    BigDecimal subTotalGrossMargin = BigDecimal.ZERO;
    BigDecimal subMarginRate = BigDecimal.ZERO;
    BigDecimal totalWT = BigDecimal.ZERO;

    if (saleOrderLine.getProduct() != null
        && saleOrderLine.getProduct().getCostPrice().compareTo(BigDecimal.ZERO) != 0
        && saleOrderLine.getExTaxTotal().compareTo(BigDecimal.ZERO) != 0) {

      totalWT =
          currencyService.getAmountCurrencyConvertedAtDate(
              saleOrder.getCurrency(),
              saleOrder.getCompany().getCurrency(),
              saleOrderLine.getExTaxTotal(),
              null);

      logger.debug("Total WT in company currency: {}", totalWT);
      subTotalCostPrice =
          saleOrderLine.getProduct().getCostPrice().multiply(saleOrderLine.getQty());
      logger.debug("Subtotal cost price: {}", subTotalCostPrice);
      subTotalGrossMargin = totalWT.subtract(subTotalCostPrice);
      logger.debug("Subtotal gross margin: {}", subTotalGrossMargin);
      subMarginRate =
          subTotalGrossMargin
              .divide(subTotalCostPrice, RoundingMode.HALF_EVEN)
              .multiply(new BigDecimal(100));
      logger.debug("Subtotal gross margin rate: {}", subMarginRate);
    }

    saleOrderLine.setSubTotalCostPrice(subTotalCostPrice);
    saleOrderLine.setSubTotalGrossMargin(subTotalGrossMargin);
    saleOrderLine.setSubMarginRate(subMarginRate);

    map.put("subTotalCostPrice", subTotalCostPrice);
    map.put("subTotalGrossMargin", subTotalGrossMargin);
    map.put("subMarginRate", subMarginRate);
    return map;
  }

  @Override
  public BigDecimal getAvailableStock(SaleOrderLine saleOrderLine) {
    // defined in supplychain
    return BigDecimal.ZERO;
  }

  @Override
  public void checkMultipleQty(SaleOrderLine saleOrderLine, ActionResponse response) {

    Product product = saleOrderLine.getProduct();

    if (product == null) {
      return;
    }

    productMultipleQtyService.checkMultipleQty(
        saleOrderLine.getQty(),
        product.getSaleProductMultipleQtyList(),
        product.getAllowToForceSaleQty(),
        response);
  }

  @Override
  public List<SaleOrderLine> createPackLines(Product product, SaleOrder saleOrder)
      throws AxelorException {
    List<SaleOrderLine> subLines = new ArrayList<>();

    for (PackLine packLine : product.getPackLines()) {
      SaleOrderLine subLine = this.createPackLine(packLine, saleOrder);
      subLines.add(subLine);
    }

    return subLines;
  }

  @Override
  public SaleOrderLine createPackLine(PackLine packLine, SaleOrder saleOrder)
      throws AxelorException {
    SaleOrderLine subLine = new SaleOrderLine();
    Product subProduct = packLine.getProduct();
    subLine.setProduct(subProduct);
    subLine.setProductName(subProduct.getName());
    subLine.setPrice(subProduct.getSalePrice());
    subLine.setUnit(this.getSaleUnit(subLine));
    subLine.setQty(new BigDecimal(packLine.getQuantity()));
    subLine.setCompanyCostPrice(this.getCompanyCostPrice(saleOrder, subLine));
    TaxLine taxLine = this.getTaxLine(saleOrder, subLine);
    subLine.setTaxLine(taxLine);

    this.computeValues(saleOrder, subLine);

    BigDecimal price = this.getUnitPrice(saleOrder, subLine, taxLine);

    Map<String, Object> discounts = this.getDiscount(saleOrder, subLine, price);

    if (discounts != null) {
      subLine.setDiscountAmount((BigDecimal) discounts.get("discountAmount"));
      subLine.setDiscountTypeSelect((Integer) discounts.get("discountTypeSelect"));
      if (discounts.get("price") != null) {
        price = (BigDecimal) discounts.get("price");
      }
    }
    subLine.setPrice(price);

    return subLine;
  }
}
