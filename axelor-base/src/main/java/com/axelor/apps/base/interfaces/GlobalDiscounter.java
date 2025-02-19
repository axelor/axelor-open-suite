package com.axelor.apps.base.interfaces;

import com.axelor.apps.base.db.Currency;
import java.math.BigDecimal;

public interface GlobalDiscounter {

  Integer getDiscountTypeSelect();

  void setPriceBeforeGlobalDiscount(BigDecimal priceBeforeGlobalDiscount);

  BigDecimal getPriceBeforeGlobalDiscount();

  BigDecimal getDiscountAmount();

  BigDecimal getExTaxTotal();

  Currency getCurrency();
}
