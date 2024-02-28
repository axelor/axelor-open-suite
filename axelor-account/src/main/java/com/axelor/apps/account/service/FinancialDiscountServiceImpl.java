/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.repo.FinancialDiscountRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.app.AppBaseService;
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
    BigDecimal financialDiscountTotalAmount = inTaxTotal;

    if (financialDiscount.getDiscountBaseSelect()
        != FinancialDiscountRepository.DISCOUNT_BASE_VAT) {
      financialDiscountTotalAmount = inTaxTotal.subtract(taxTotal);
    }
    return this.getFinancialDiscountAmount(
        financialDiscount, financialDiscountTotalAmount, currency);
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

  @Override
  public BigDecimal computeRemainingAmountAfterFinDiscount(
      FinancialDiscount financialDiscount,
      BigDecimal inTaxTotal,
      BigDecimal exTaxTotal,
      BigDecimal taxTotal,
      BigDecimal financialDiscountTotalAmount,
      Currency currency) {
    if (financialDiscount.getDiscountBaseSelect()
        == FinancialDiscountRepository.DISCOUNT_BASE_VAT) {
      return inTaxTotal.subtract(financialDiscountTotalAmount);
    } else {
      BigDecimal financialDiscountAmountWithoutTax =
          exTaxTotal.subtract(financialDiscountTotalAmount);
      BigDecimal financialDiscountTaxAmount =
          financialDiscountAmountWithoutTax.multiply(
              taxTotal.divide(
                  exTaxTotal, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP));

      return currencyScaleServiceAccount.getScaledValue(
          financialDiscountAmountWithoutTax.add(financialDiscountTaxAmount),
          currency.getNumberOfDecimals());
    }
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
