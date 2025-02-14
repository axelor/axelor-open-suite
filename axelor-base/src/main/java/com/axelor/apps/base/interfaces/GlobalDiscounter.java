package com.axelor.apps.base.interfaces;

import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.TemporaryLineHolder;
import java.math.BigDecimal;

public interface GlobalDiscounter {

  TemporaryLineHolder getTemporaryLineHolder();

  Integer getDiscountTypeSelect();

  void setPriceBeforeGlobalDiscount(BigDecimal priceBeforeGlobalDiscount);

  BigDecimal getPriceBeforeGlobalDiscount();

  BigDecimal getDiscountAmount();

  BigDecimal getExTaxTotal();

  Currency getCurrency();
}
