package com.axelor.apps.base.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProductPriceServiceImpl implements ProductPriceService {
  protected AppBaseService appBaseService;

  protected CurrencyService currencyService;
  protected ProductCompanyService productCompanyService;

  protected TaxService taxService;

  @Inject
  public ProductPriceServiceImpl(
      CurrencyService currencyService,
      ProductCompanyService productCompanyService,
      TaxService taxService,
      AppBaseService appBaseService) {

    this.currencyService = currencyService;
    this.productCompanyService = productCompanyService;
    this.appBaseService = appBaseService;
    this.taxService = taxService;
  }

  @Override
  public Map<String, Object> getSaleUnitPrice(Product product, Company company)
      throws AxelorException {
    Map<String, Object> map = new HashMap<>();

    BigDecimal price = (BigDecimal) productCompanyService.get(product, "salePrice", company);
    Currency currency = (Currency) productCompanyService.get(product, "saleCurrency", company);
    map.put("price", price);
    map.put("currency", currency);
    return map;
  }

  @Override
  public Map<String, Object> getPurchaseUnitPrice(Product product, Company company)
      throws AxelorException {
    Map<String, Object> map = new HashMap<>();

    BigDecimal price = (BigDecimal) productCompanyService.get(product, "purchasePrice", company);
    Currency currency = (Currency) productCompanyService.get(product, "purchaseCurrency", company);
    map.put("price", price);
    map.put("currency", currency);
    return map;
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
