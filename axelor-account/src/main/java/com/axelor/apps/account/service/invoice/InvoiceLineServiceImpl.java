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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.AppAccountRepository;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceLineServiceImpl implements InvoiceLineService {

  protected AccountManagementService accountManagementService;
  protected AccountManagementAccountService accountManagementAccountService;
  protected CurrencyService currencyService;
  protected PriceListService priceListService;
  protected AppAccountService appAccountService;
  protected AnalyticMoveLineService analyticMoveLineService;

  @Inject
  public InvoiceLineServiceImpl(
      AccountManagementService accountManagementService,
      CurrencyService currencyService,
      PriceListService priceListService,
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      AccountManagementAccountService accountManagementAccountService) {

    this.accountManagementService = accountManagementService;
    this.accountManagementAccountService = accountManagementAccountService;
    this.currencyService = currencyService;
    this.priceListService = priceListService;
    this.appAccountService = appAccountService;
    this.analyticMoveLineService = analyticMoveLineService;
  }

  @Override
  public InvoiceLine computeAnalyticDistribution(InvoiceLine invoiceLine) throws AxelorException {

    if (appAccountService.getAppAccount().getAnalyticDistributionTypeSelect()
        == AppAccountRepository.DISTRIBUTION_TYPE_FREE) {
      return invoiceLine;
    }

    Invoice invoice = invoiceLine.getInvoice();
    List<AnalyticMoveLine> analyticMoveLineList = invoiceLine.getAnalyticMoveLineList();
    if ((analyticMoveLineList == null || analyticMoveLineList.isEmpty())) {
      analyticMoveLineList =
          analyticMoveLineService.generateLines(
              invoice.getPartner(),
              invoiceLine.getProduct(),
              invoice.getCompany(),
              invoiceLine.getExTaxTotal());
      invoiceLine.setAnalyticMoveLineList(analyticMoveLineList);
    }
    if (analyticMoveLineList != null) {
      for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
        this.updateAnalyticMoveLine(analyticMoveLine, invoiceLine);
      }
    }
    return invoiceLine;
  }

  @Override
  public void updateAnalyticMoveLine(AnalyticMoveLine analyticMoveLine, InvoiceLine invoiceLine) {

    analyticMoveLine.setInvoiceLine(invoiceLine);
    analyticMoveLine.setAmount(analyticMoveLineService.computeAmount(analyticMoveLine));
    analyticMoveLine.setDate(appAccountService.getTodayDate());
    analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_FORECAST_INVOICE);
  }

  @Override
  public InvoiceLine createAnalyticDistributionWithTemplate(InvoiceLine invoiceLine)
      throws AxelorException {
    List<AnalyticMoveLine> analyticMoveLineList = null;
    analyticMoveLineList =
        analyticMoveLineService.generateLinesWithTemplate(
            invoiceLine.getAnalyticDistributionTemplate(), invoiceLine.getExTaxTotal());
    if (analyticMoveLineList != null) {
      for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
        analyticMoveLine.setInvoiceLine(invoiceLine);
      }
    }
    invoiceLine.setAnalyticMoveLineList(analyticMoveLineList);
    return invoiceLine;
  }

  @Override
  public TaxLine getTaxLine(Invoice invoice, InvoiceLine invoiceLine, boolean isPurchase)
      throws AxelorException {

    return accountManagementService.getTaxLine(
        appAccountService.getTodayDate(),
        invoiceLine.getProduct(),
        invoice.getCompany(),
        invoice.getPartner().getFiscalPosition(),
        isPurchase);
  }

  @Override
  public BigDecimal getExTaxUnitPrice(
      Invoice invoice, InvoiceLine invoiceLine, TaxLine taxLine, boolean isPurchase)
      throws AxelorException {

    return this.getUnitPrice(invoice, invoiceLine, taxLine, isPurchase, false);
  }

  @Override
  public BigDecimal getInTaxUnitPrice(
      Invoice invoice, InvoiceLine invoiceLine, TaxLine taxLine, boolean isPurchase)
      throws AxelorException {

    return this.getUnitPrice(invoice, invoiceLine, taxLine, isPurchase, true);
  }

  /**
   * A function used to get the unit price of an invoice line, either in ati or wt
   *
   * @param invoice the invoice containing the invoice line
   * @param invoiceLine
   * @param taxLine the tax line applied to the unit price
   * @param isPurchase
   * @param resultInAti whether or not you want the result unit price in ati
   * @return the unit price of the invoice line
   * @throws AxelorException
   */
  private BigDecimal getUnitPrice(
      Invoice invoice,
      InvoiceLine invoiceLine,
      TaxLine taxLine,
      boolean isPurchase,
      boolean resultInAti)
      throws AxelorException {
    Product product = invoiceLine.getProduct();

    BigDecimal price = null;
    Currency productCurrency;

    if (isPurchase) {
      price = product.getPurchasePrice();
      productCurrency = product.getPurchaseCurrency();
    } else {
      price = product.getSalePrice();
      productCurrency = product.getSaleCurrency();
    }

    if (product.getInAti() != resultInAti) {
      price = this.convertUnitPrice(product.getInAti(), taxLine, price);
    }

    return currencyService
        .getAmountCurrencyConvertedAtDate(
            productCurrency, invoice.getCurrency(), price, invoice.getInvoiceDate())
        .setScale(appAccountService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
  }

  @Override
  public boolean isPurchase(Invoice invoice) {
    return invoice.getOperationTypeSelect() == 1 || invoice.getOperationTypeSelect() == 2;
  }

  @Override
  public BigDecimal getAccountingExTaxTotal(BigDecimal exTaxTotal, Invoice invoice)
      throws AxelorException {

    return currencyService
        .getAmountCurrencyConvertedAtDate(
            invoice.getCurrency(),
            invoice.getPartner().getCurrency(),
            exTaxTotal,
            invoice.getInvoiceDate())
        .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal getCompanyExTaxTotal(BigDecimal exTaxTotal, Invoice invoice)
      throws AxelorException {

    return currencyService
        .getAmountCurrencyConvertedAtDate(
            invoice.getCurrency(),
            invoice.getCompany().getCurrency(),
            exTaxTotal,
            invoice.getInvoiceDate())
        .setScale(appAccountService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
  }

  @Override
  public PriceListLine getPriceListLine(InvoiceLine invoiceLine, PriceList priceList) {

    return priceListService.getPriceListLine(
        invoiceLine.getProduct(), invoiceLine.getQty(), priceList);
  }

  @Override
  public BigDecimal computeDiscount(InvoiceLine invoiceLine, Boolean inAti) {

    BigDecimal unitPrice = inAti ? invoiceLine.getInTaxPrice() : invoiceLine.getPrice();

    return priceListService.computeDiscount(
        unitPrice, invoiceLine.getDiscountTypeSelect(), invoiceLine.getDiscountAmount());
  }

  @Override
  public BigDecimal convertUnitPrice(Boolean priceIsAti, TaxLine taxLine, BigDecimal price) {

    if (taxLine == null) {
      return price;
    }

    if (priceIsAti) {
      price = price.divide(taxLine.getValue().add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
    } else {
      price = price.add(price.multiply(taxLine.getValue()));
    }
    return price;
  }

  @Override
  public Map<String, Object> getDiscount(Invoice invoice, InvoiceLine invoiceLine, BigDecimal price)
      throws AxelorException {

    Map<String, Object> rawDiscounts = getDiscountsFromPriceLists(invoice, invoiceLine, price);

    Map<String, Object> processedDiscounts = new HashMap<>();

    if (rawDiscounts != null) {
      if (rawDiscounts.get("price") != null) {
        price = (BigDecimal) rawDiscounts.get("price");
      }
      if (invoiceLine.getProduct().getInAti() != invoice.getInAti()
          && (Integer) rawDiscounts.get("discountTypeSelect")
              != PriceListLineRepository.AMOUNT_TYPE_PERCENT) {
        processedDiscounts.put(
            "discountAmount",
            this.convertUnitPrice(
                invoiceLine.getProduct().getInAti(),
                invoiceLine.getTaxLine(),
                (BigDecimal) rawDiscounts.get("discountAmount")));
      } else {
        processedDiscounts.put("discountAmount", rawDiscounts.get("discountAmount"));
      }
      processedDiscounts.put("discountTypeSelect", rawDiscounts.get("discountTypeSelect"));
    }

    if (price.compareTo(
            invoiceLine.getProduct().getInAti()
                ? invoiceLine.getInTaxPrice()
                : invoiceLine.getPrice())
        != 0) {
      if (invoiceLine.getProduct().getInAti()) {
        processedDiscounts.put("inTaxPrice", price);
        processedDiscounts.put(
            "price", this.convertUnitPrice(true, invoiceLine.getTaxLine(), price));
      } else {
        processedDiscounts.put("price", price);
        processedDiscounts.put(
            "inTaxPrice", this.convertUnitPrice(false, invoiceLine.getTaxLine(), price));
      }
    }

    return processedDiscounts;
  }

  @Override
  public Map<String, Object> getDiscountsFromPriceLists(
      Invoice invoice, InvoiceLine invoiceLine, BigDecimal price) {

    Map<String, Object> discounts = null;

    PriceList priceList = invoice.getPriceList();

    if (priceList != null) {
      PriceListLine priceListLine = this.getPriceListLine(invoiceLine, priceList);
      discounts = priceListService.getReplacedPriceAndDiscounts(priceList, priceListLine, price);
    }

    return discounts;
  }

  @Override
  public int getDiscountTypeSelect(Invoice invoice, InvoiceLine invoiceLine) {
    PriceList priceList = invoice.getPriceList();
    if (priceList != null) {
      PriceListLine priceListLine = this.getPriceListLine(invoiceLine, priceList);

      return priceListLine.getTypeSelect();
    }
    return 0;
  }

  @Override
  public Unit getUnit(Product product, boolean isPurchase) {
    return product.getUnit();
  }

  @Override
  public Map<String, Object> resetProductInformation() {
    Map<String, Object> productInformation = new HashMap<>();
    productInformation.put("taxLine", null);
    productInformation.put("taxEquiv", null);
    productInformation.put("taxCode", null);
    productInformation.put("taxRate", null);
    productInformation.put("productName", null);
    productInformation.put("unit", null);
    productInformation.put("discountAmount", null);
    productInformation.put("discountTypeSelect", null);
    productInformation.put("price", null);
    productInformation.put("inTaxPrice", null);
    productInformation.put("exTaxTotal", null);
    productInformation.put("inTaxTotal", null);
    productInformation.put("companyInTaxTotal", null);
    productInformation.put("companyExTaxTotal", null);
    return productInformation;
  }

  @Override
  public Map<String, Object> fillProductInformation(Invoice invoice, InvoiceLine invoiceLine)
      throws AxelorException {
    Map<String, Object> productInformation = new HashMap<>();

    Product product = invoiceLine.getProduct();
    boolean isPurchase = this.isPurchase(invoice);
    TaxLine taxLine;

    try {
      taxLine = getTaxLine(invoice, invoiceLine, isPurchase);
      invoiceLine.setTaxLine(taxLine);
      productInformation.put("taxLine", taxLine);
      productInformation.put("taxRate", taxLine.getValue());
      productInformation.put("taxCode", taxLine.getTax().getCode());

      AccountManagement accountManagement =
          accountManagementAccountService.getAccountManagement(product, invoice.getCompany());

      Tax tax = accountManagementAccountService.getProductTax(accountManagement, isPurchase);
      TaxEquiv taxEquiv =
          Beans.get(FiscalPositionService.class)
              .getTaxEquiv(invoice.getPartner().getFiscalPosition(), tax);
      productInformation.put("taxEquiv", taxEquiv);

      // getting correct account for the product
      Account account =
          accountManagementAccountService.getProductAccount(accountManagement, isPurchase);
      productInformation.put("account", account);
    } catch (AxelorException e) {
      taxLine = null;
      productInformation.put("taxLine", null);
      productInformation.put("taxRate", null);
      productInformation.put("taxCode", null);
      productInformation.put("taxEquiv", null);
      productInformation.put("account", null);
      productInformation.put("error", e.getMessage());
    }

    BigDecimal price = this.getExTaxUnitPrice(invoice, invoiceLine, taxLine, isPurchase);
    BigDecimal inTaxPrice = this.getInTaxUnitPrice(invoice, invoiceLine, taxLine, isPurchase);

    productInformation.put("productName", invoiceLine.getProduct().getName());
    productInformation.put("productCode", invoiceLine.getProduct().getCode());
    productInformation.put("unit", this.getUnit(invoiceLine.getProduct(), isPurchase));

    productInformation.put("price", price);
    productInformation.put("inTaxPrice", inTaxPrice);

    productInformation.putAll(
        this.getDiscount(invoice, invoiceLine, product.getInAti() ? inTaxPrice : price));

    productInformation.put("productName", invoiceLine.getProduct().getName());

    return productInformation;
  }
}
