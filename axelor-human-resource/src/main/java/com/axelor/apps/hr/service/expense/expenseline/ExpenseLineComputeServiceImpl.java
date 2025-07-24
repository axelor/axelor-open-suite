package com.axelor.apps.hr.service.expense.expenseline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.google.inject.Inject;
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
