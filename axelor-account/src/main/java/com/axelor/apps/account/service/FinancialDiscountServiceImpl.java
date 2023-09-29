package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.repo.FinancialDiscountRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class FinancialDiscountServiceImpl implements FinancialDiscountService {
  AccountConfigService accountConfigService;

  @Override
  public BigDecimal computeFinancialDiscountTotalAmount(
      FinancialDiscount financialDiscount, BigDecimal inTaxTotal, BigDecimal taxTotal) {
    BigDecimal exTaxTotal = inTaxTotal.subtract(taxTotal);

    if (financialDiscount.getDiscountBaseSelect()
        == FinancialDiscountRepository.DISCOUNT_BASE_VAT) {
      return this.getFinancialDiscountAmount(financialDiscount, inTaxTotal);
    } else {
      BigDecimal financialDiscountAmountWithoutTax =
          this.getFinancialDiscountAmount(financialDiscount, exTaxTotal);

      BigDecimal financialDiscountTaxAmount =
          this.getFinancialDiscountTaxAmount(financialDiscount, inTaxTotal, exTaxTotal, taxTotal);

      return financialDiscountAmountWithoutTax.add(financialDiscountTaxAmount);
    }
  }

  protected BigDecimal getFinancialDiscountAmount(
      FinancialDiscount financialDiscount, BigDecimal amount) {
    return financialDiscount
        .getDiscountRate()
        .multiply(amount)
        .divide(
            new BigDecimal(100), AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
  }

  protected BigDecimal getFinancialDiscountTaxAmount(
      FinancialDiscount financialDiscount,
      BigDecimal inTaxTotal,
      BigDecimal exTaxTotal,
      BigDecimal taxTotal) {
    return taxTotal
        .multiply(exTaxTotal)
        .multiply(financialDiscount.getDiscountRate())
        .divide(
            inTaxTotal.multiply(BigDecimal.valueOf(100)),
            AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
            RoundingMode.HALF_UP);
  }

  @Override
  public Account getFinancialDiscountAccount(Company company, boolean isPurchase)
      throws AxelorException {
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    if (isPurchase) {
      return accountConfigService.getPurchFinancialDiscountAccount(accountConfig);
    } else {
      return accountConfigService.getSaleFinancialDiscountAccount(accountConfig);
    }
  }
}
