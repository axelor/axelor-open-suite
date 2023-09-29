package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import java.math.BigDecimal;

public interface FinancialDiscountService {
  BigDecimal computeFinancialDiscountTotalAmount(
      FinancialDiscount financialDiscount, BigDecimal inTaxTotal, BigDecimal taxTotal);

  Account getFinancialDiscountAccount(Company company, boolean isPurchase) throws AxelorException;
}
