package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.hr.db.Expense;
import com.google.inject.Inject;
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
