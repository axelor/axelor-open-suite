package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import java.math.BigDecimal;

public interface ProductPriceListService {

  BigDecimal applyPriceList(
      Product product,
      Partner partner,
      Company company,
      Currency currency,
      BigDecimal price,
      boolean inAti)
      throws AxelorException;
}
