package com.axelor.apps.base.service.discount;

import com.axelor.apps.base.db.Currency;
import java.math.BigDecimal;
import java.util.Map;

public interface GlobalDiscountService {
  BigDecimal computeDiscountFixedEquivalence(
      BigDecimal exTaxTotal, BigDecimal priceBeforeGlobalDiscount);

  BigDecimal computeDiscountPercentageEquivalence(
      BigDecimal exTaxTotal, BigDecimal priceBeforeGlobalDiscount);

  Map<String, Map<String, Object>> setDiscountDummies(
      Integer discountTypeSelect,
      Currency currency,
      BigDecimal exTaxTotal,
      BigDecimal priceBeforeGlobalDiscount);
}
