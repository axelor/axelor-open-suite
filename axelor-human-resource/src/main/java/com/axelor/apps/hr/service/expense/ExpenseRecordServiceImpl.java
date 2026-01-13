/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.hr.db.Expense;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ExpenseRecordServiceImpl implements ExpenseRecordService {

  protected CurrencyService currencyService;

  @Inject
  public ExpenseRecordServiceImpl(CurrencyService currencyService) {
    this.currencyService = currencyService;
  }

  @Override
  public Map<String, Object> computeDummyAmounts(Expense expense) throws AxelorException {
    Map<String, Object> values = new HashMap<>();

    BigDecimal totalImputed = computeTotalImputed(expense);
    values.put("$totalToRefund", expense.getInTaxTotal().subtract(totalImputed));

    computedCompanyAmounts(values, expense, totalImputed);

    return values;
  }

  protected void computedCompanyAmounts(
      Map<String, Object> values, Expense expense, BigDecimal totalImputed) throws AxelorException {
    Currency companyCurrency =
        Optional.of(expense).map(Expense::getCompany).map(Company::getCurrency).orElse(null);

    if (Objects.equals(companyCurrency, expense.getCurrency())) {
      return;
    }

    BigDecimal companyTotalImputed =
        currencyService.getAmountCurrencyConvertedAtDate(
            expense.getCurrency(), companyCurrency, totalImputed, expense.getPaymentDate());
    values.put(
        "$companyTotalToRefund", expense.getCompanyInTaxTotal().subtract(companyTotalImputed));
    values.put(
        "$companyPaymentAmount",
        currencyService.getAmountCurrencyConvertedAtDate(
            expense.getCurrency(),
            companyCurrency,
            expense.getPaymentAmount(),
            expense.getPaymentDate()));
    values.put(
        "$companyPersonalExpenseAmount",
        currencyService.getAmountCurrencyConvertedAtDate(
            expense.getCurrency(),
            companyCurrency,
            expense.getPersonalExpenseAmount(),
            expense.getMoveDate()));
    values.put(
        "$companyWithdrawnCash",
        currencyService.getAmountCurrencyConvertedAtDate(
            expense.getCurrency(),
            companyCurrency,
            expense.getWithdrawnCash(),
            expense.getMoveDate()));
    values.put(
        "$companyAdvanceAmount",
        currencyService.getAmountCurrencyConvertedAtDate(
            expense.getCurrency(),
            companyCurrency,
            expense.getAdvanceAmount(),
            expense.getMoveDate()));
  }

  protected BigDecimal computeTotalImputed(Expense expense) {
    if (expense == null) {
      return BigDecimal.ZERO;
    }

    return expense
        .getAdvanceAmount()
        .add(expense.getWithdrawnCash())
        .add(expense.getPersonalExpenseAmount())
        .add(expense.getPaymentAmount());
  }
}
