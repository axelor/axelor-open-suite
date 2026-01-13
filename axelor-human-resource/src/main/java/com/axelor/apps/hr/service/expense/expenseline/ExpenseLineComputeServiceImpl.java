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
package com.axelor.apps.hr.service.expense.expenseline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public class ExpenseLineComputeServiceImpl implements ExpenseLineComputeService {

  protected CurrencyService currencyService;

  @Inject
  public ExpenseLineComputeServiceImpl(CurrencyService currencyService) {
    this.currencyService = currencyService;
  }

  @Override
  public void computeUntaxedAndCompanyAmounts(ExpenseLine expenseLine, Expense expense)
      throws AxelorException {
    if (expenseLine == null) {
      return;
    }

    expenseLine.setUntaxedAmount(expenseLine.getTotalAmount().subtract(expenseLine.getTotalTax()));
    setCompanyAmounts(expenseLine, expense);
  }

  @Override
  public void setCompanyAmounts(ExpenseLine expenseLine, Expense expense) throws AxelorException {
    expense = Optional.ofNullable(expense).orElse(expenseLine.getExpense());
    Currency companyCurrency =
        Optional.ofNullable(expense)
            .map(Expense::getCompany)
            .map(Company::getCurrency)
            .orElse(null);
    Currency expenseCurrency = Optional.ofNullable(expense).map(Expense::getCurrency).orElse(null);
    BigDecimal companyUntaxedAmount = expenseLine.getUntaxedAmount();
    BigDecimal companyTotalTax = expenseLine.getTotalTax();
    BigDecimal companyTotalAmount = expenseLine.getTotalAmount();

    if (!Objects.equals(companyCurrency, expenseCurrency)) {
      LocalDate date = expenseLine.getExpenseDate();
      companyTotalTax =
          currencyService.getAmountCurrencyConvertedAtDate(
              expenseCurrency, companyCurrency, companyTotalTax, date);
      companyTotalAmount =
          currencyService.getAmountCurrencyConvertedAtDate(
              expenseCurrency, companyCurrency, companyTotalAmount, date);
      companyUntaxedAmount = companyTotalAmount.subtract(companyTotalTax);
    }

    expenseLine.setCompanyUntaxedAmount(companyUntaxedAmount);
    expenseLine.setCompanyTotalTax(companyTotalTax);
    expenseLine.setCompanyTotalAmount(companyTotalAmount);
  }
}
