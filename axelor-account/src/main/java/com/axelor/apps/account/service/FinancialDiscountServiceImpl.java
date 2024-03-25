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
import com.axelor.apps.base.service.CurrencyScaleService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class FinancialDiscountServiceImpl implements FinancialDiscountService {
  AccountConfigService accountConfigService;
  CurrencyScaleService currencyScaleService;

  @Inject
  public FinancialDiscountServiceImpl(
      AccountConfigService accountConfigService, CurrencyScaleService currencyScaleService) {
    this.accountConfigService = accountConfigService;
    this.currencyScaleService = currencyScaleService;
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
            currencyScaleService.getCurrencyScale(currency),
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
                currencyScaleService.getCurrencyScale(currency),
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
