package com.axelor.apps.base.service;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.base.service.tax.TaxService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Set;

public class ProductPriceServiceImpl implements ProductPriceService {
  protected AppBaseService appBaseService;
  protected FiscalPositionService fiscalPositionService;
  protected CurrencyService currencyService;
  protected ProductCompanyService productCompanyService;

  protected TaxService taxService;
  protected AccountManagementService accountManagementService;

  @Inject
  public ProductPriceServiceImpl(
      CurrencyService currencyService,
      ProductCompanyService productCompanyService,
      TaxService taxService,
      AppBaseService appBaseService,
      AccountManagementService accountManagementService,
      FiscalPositionService fiscalPositionService) {

    this.currencyService = currencyService;
    this.productCompanyService = productCompanyService;
    this.appBaseService = appBaseService;
    this.taxService = taxService;
    this.accountManagementService = accountManagementService;
    this.fiscalPositionService = fiscalPositionService;
  }

  @Override
  public BigDecimal getSaleUnitPrice(Company company, Product product) throws AxelorException {
    return getSaleUnitPrice(company, product, product.getInAti(), null);
  }

  @Override
  public BigDecimal getSaleUnitPrice(
      Company company, Product product, Boolean inAti, Partner partner) throws AxelorException {
    LocalDate todayDate = appBaseService.getTodayDate(company);
    Currency toCurrency = (Currency) productCompanyService.get(product, "saleCurrency", company);

    FiscalPosition fiscalPosition = null;
    if (partner != null) {
      fiscalPosition = partner.getFiscalPosition();
      if (partner.getCurrency() != null) {
        toCurrency = partner.getCurrency();
      }
    }
    Set<TaxLine> taxLineSet =
        accountManagementService.getTaxLineSet(todayDate, product, company, fiscalPosition, false);

    return getSaleUnitPrice(company, product, taxLineSet, inAti, todayDate, toCurrency);
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
    return getConvertedPrice(
        company, product, taxLineSet, resultInAti, localDate, price, currency, toCurrency);
  }

  @Override
  public BigDecimal getPurchaseUnitPrice(
      Company company,
      Product product,
      Set<TaxLine> taxLineSet,
      boolean resultInAti,
      LocalDate localDate,
      Currency toCurrency)
      throws AxelorException {
    BigDecimal price = (BigDecimal) productCompanyService.get(product, "purchasePrice", company);
    Currency currency = (Currency) productCompanyService.get(product, "purchaseCurrency", company);
    return getConvertedPrice(
        company, product, taxLineSet, resultInAti, localDate, price, currency, toCurrency);
  }

  @Override
  public BigDecimal getConvertedPrice(
      Company company,
      Product product,
      Set<TaxLine> taxLineSet,
      boolean resultInAti,
      LocalDate localDate,
      BigDecimal price,
      Currency fromCurrency,
      Currency toCurrency)
      throws AxelorException {
    if ((Boolean) productCompanyService.get(product, "inAti", company) != resultInAti) {
      price =
          taxService.convertUnitPrice(
              (Boolean) productCompanyService.get(product, "inAti", company),
              taxLineSet,
              price,
              AppBaseService.COMPUTATION_SCALING);
    }
    return currencyService
        .getAmountCurrencyConvertedAtDate(fromCurrency, toCurrency, price, localDate)
        .setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
  }
}
