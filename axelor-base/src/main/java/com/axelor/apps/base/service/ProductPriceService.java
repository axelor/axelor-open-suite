package com.axelor.apps.base.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Product;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

public interface ProductPriceService {

  Map<String, Object> getSaleUnitPrice(Product product, Company company) throws AxelorException;

  Map<String, Object> getPurchaseUnitPrice(Product product, Company company) throws AxelorException;

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
