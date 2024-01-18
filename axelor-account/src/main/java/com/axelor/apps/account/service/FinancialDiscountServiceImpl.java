package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.repo.FinancialDiscountRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class FinancialDiscountServiceImpl implements FinancialDiscountService {
  AccountConfigService accountConfigService;
  CurrencyScaleServiceAccount currencyScaleServiceAccount;

  @Inject
  public FinancialDiscountServiceImpl(
      AccountConfigService accountConfigService,
      CurrencyScaleServiceAccount currencyScaleServiceAccount) {
    this.accountConfigService = accountConfigService;
    this.currencyScaleServiceAccount = currencyScaleServiceAccount;
  }

  @Override
  public BigDecimal computeFinancialDiscountTotalAmount(
      FinancialDiscount financialDiscount,
      BigDecimal inTaxTotal,
      BigDecimal taxTotal,
      Currency currency) {
    BigDecimal exTaxTotal = inTaxTotal.subtract(taxTotal);

    if (financialDiscount.getDiscountBaseSelect()
        == FinancialDiscountRepository.DISCOUNT_BASE_VAT) {
      return this.getFinancialDiscountAmount(financialDiscount, inTaxTotal, currency);
    } else {
      BigDecimal financialDiscountAmountWithoutTax =
          this.getFinancialDiscountAmount(financialDiscount, exTaxTotal, currency);

      BigDecimal financialDiscountTaxAmount =
          this.getFinancialDiscountTaxAmount(
              financialDiscount, inTaxTotal, exTaxTotal, taxTotal, currency);

      return financialDiscountAmountWithoutTax.add(financialDiscountTaxAmount);
    }
  }

  protected BigDecimal getFinancialDiscountAmount(
      FinancialDiscount financialDiscount, BigDecimal amount, Currency currency) {
    return financialDiscount
        .getDiscountRate()
        .multiply(amount)
        .divide(
            new BigDecimal(100),
            currencyScaleServiceAccount.getScale(currency),
            RoundingMode.HALF_UP);
  }

  protected BigDecimal getFinancialDiscountTaxAmount(
      FinancialDiscount financialDiscount,
      BigDecimal inTaxTotal,
      BigDecimal exTaxTotal,
      BigDecimal taxTotal,
      Currency currency) {
    return inTaxTotal.signum() == 0
        ? BigDecimal.ZERO
        : taxTotal
            .multiply(exTaxTotal)
            .multiply(financialDiscount.getDiscountRate())
            .divide(
                inTaxTotal.multiply(BigDecimal.valueOf(100)),
                currencyScaleServiceAccount.getScale(currency),
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
