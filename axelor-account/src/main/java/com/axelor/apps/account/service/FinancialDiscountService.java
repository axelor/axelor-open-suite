package com.axelor.apps.account.service;

import com.axelor.apps.account.db.FinancialDiscount;
import java.math.BigDecimal;

public interface FinancialDiscountService {
  BigDecimal computeFinancialDiscountTotalAmount(
      FinancialDiscount financialDiscount, BigDecimal inTaxTotal, BigDecimal taxTotal);
}
