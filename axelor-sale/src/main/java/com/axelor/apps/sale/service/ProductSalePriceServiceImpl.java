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
package com.axelor.apps.sale.service;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ProductPriceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.base.service.tax.TaxService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Set;

public class ProductSalePriceServiceImpl implements ProductSalePriceService {
  protected AppBaseService appBaseService;
  protected FiscalPositionService fiscalPositionService;
  protected CurrencyService currencyService;
  protected ProductCompanyService productCompanyService;

  protected TaxService taxService;
  protected AccountManagementService accountManagementService;
  protected ProductPriceListService productPriceListService;
  protected ProductPriceService productPriceService;

  @Inject
  public ProductSalePriceServiceImpl(
      CurrencyService currencyService,
      ProductCompanyService productCompanyService,
      TaxService taxService,
      AppBaseService appBaseService,
      AccountManagementService accountManagementService,
      FiscalPositionService fiscalPositionService,
      ProductPriceService productPriceService) {

    this.currencyService = currencyService;
    this.productCompanyService = productCompanyService;
    this.appBaseService = appBaseService;
    this.taxService = taxService;
    this.accountManagementService = accountManagementService;
    this.fiscalPositionService = fiscalPositionService;
    this.productPriceService = productPriceService;
  }

  @Override
  public BigDecimal getSaleUnitPrice(Company company, Product product) throws AxelorException {
    return getSaleUnitPrice(company, product, product.getInAti(), null);
  }

  @Override
  public BigDecimal getSaleUnitPrice(
      Company company, Product product, boolean inAti, Partner partner) throws AxelorException {
    return getSaleUnitPrice(company, product, inAti, partner, null);
  }

  @Override
  public BigDecimal getSaleUnitPrice(
      Company company, Product product, boolean inAti, Partner partner, Currency currency)
      throws AxelorException {
    LocalDate todayDate = appBaseService.getTodayDate(company);

    Currency toCurrency = (Currency) productCompanyService.get(product, "saleCurrency", company);
    FiscalPosition fiscalPosition = null;
    if (partner != null) {
      fiscalPosition = partner.getFiscalPosition();
      if (partner.getCurrency() != null) {
        toCurrency = partner.getCurrency();
      }
    }
    if (currency != null) {
      toCurrency = currency;
    }
    Set<TaxLine> taxLineSet =
        accountManagementService.getTaxLineSet(todayDate, product, company, fiscalPosition, false);
    if (partner == null) {
      return getSaleUnitPrice(company, product, taxLineSet, inAti, todayDate, toCurrency);
    }
    BigDecimal priceWT =
        productPriceListService.applyPriceList(product, partner, company, currency, false);
    if (!inAti) {
      return priceWT;
    }
    return getInTaxPrice(product, company, partner, priceWT);
  }

  @Override
  public BigDecimal getSaleUnitPrice(
      Company company,
      Product product,
      Set<TaxLine> taxLineSet,
      boolean resultInAti,
      LocalDate localDate,
      Currency toCurrency)
      throws AxelorException {
    BigDecimal price = (BigDecimal) productCompanyService.get(product, "salePrice", company);
    Currency currency = (Currency) productCompanyService.get(product, "saleCurrency", company);
    return productPriceService.getConvertedPrice(
        company, product, taxLineSet, resultInAti, localDate, price, currency, toCurrency);
  }

  BigDecimal getInTaxPrice(Product product, Company company, Partner partner, BigDecimal exTaxPrice)
      throws AxelorException {
    Set<TaxLine> taxLineSet =
        accountManagementService.getTaxLineSet(
            appBaseService.getTodayDate(company),
            product,
            company,
            partner.getFiscalPosition(),
            false);
    BigDecimal taxRate = taxService.getTotalTaxRate(taxLineSet);
    return exTaxPrice
        .add(exTaxPrice.multiply(taxRate))
        .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
  }
}
