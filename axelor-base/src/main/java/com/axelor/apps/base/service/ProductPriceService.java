package com.axelor.apps.base.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public interface ProductPriceService {
  BigDecimal getSaleUnitPrice(Company company, Product product) throws AxelorException;

  BigDecimal getSaleUnitPrice(Company company, Product product, Boolean inAti, Partner partner)
      throws AxelorException;

  BigDecimal getSaleUnitPrice(
      Company company,
      Product product,
      Set<TaxLine> taxLineSet,
      boolean resultInAti,
      LocalDate localDate,
      Currency toCurrency)
      throws AxelorException;

  BigDecimal getPurchaseUnitPrice(
      Company company,
      Product product,
      Set<TaxLine> taxLineSet,
      boolean resultInAti,
      LocalDate localDate,
      Currency toCurrency)
      throws AxelorException;

  BigDecimal getConvertedPrice(
      Company company,
      Product product,
      Set<TaxLine> taxLineSet,
      boolean resultInAti,
      LocalDate localDate,
      BigDecimal price,
      Currency fromCurrency,
      Currency toCurrency)
      throws AxelorException;
}
