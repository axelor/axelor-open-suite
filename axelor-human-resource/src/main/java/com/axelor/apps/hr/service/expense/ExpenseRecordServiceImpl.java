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

    Currency companyCurrency =
        Optional.of(expense).map(Expense::getCompany).map(Company::getCurrency).orElse(null);

    BigDecimal companyTotalImputed = BigDecimal.ZERO;
    if (!Objects.equals(companyCurrency, expense.getCurrency())) {
      companyTotalImputed =
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
    }

    return values;
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
