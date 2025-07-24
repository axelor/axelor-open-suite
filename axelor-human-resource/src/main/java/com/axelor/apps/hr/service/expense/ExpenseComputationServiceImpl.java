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
import com.axelor.apps.hr.db.EmployeeAdvanceUsage;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Singleton
public class ExpenseComputationServiceImpl implements ExpenseComputationService {

  protected ExpenseLineService expenseLineService;
  protected CurrencyService currencyService;

  @Inject
  public ExpenseComputationServiceImpl(
      ExpenseLineService expenseLineService, CurrencyService currencyService) {
    this.expenseLineService = expenseLineService;
    this.currencyService = currencyService;
  }

  @Override
  public Expense compute(Expense expense) {

    BigDecimal exTaxTotal = BigDecimal.ZERO;
    BigDecimal taxTotal = BigDecimal.ZERO;
    BigDecimal inTaxTotal = BigDecimal.ZERO;
    BigDecimal companyExTaxTotal = BigDecimal.ZERO;
    BigDecimal companyTaxTotal = BigDecimal.ZERO;
    BigDecimal companyInTaxTotal = BigDecimal.ZERO;
    List<ExpenseLine> generalExpenseLineList = expense.getGeneralExpenseLineList();
    List<ExpenseLine> kilometricExpenseLineList = expense.getKilometricExpenseLineList();

    if (ObjectUtils.notEmpty(generalExpenseLineList)) {
      for (ExpenseLine expenseLine : generalExpenseLineList) {
        exTaxTotal = exTaxTotal.add(expenseLine.getUntaxedAmount());
        taxTotal = taxTotal.add(expenseLine.getTotalTax());
        inTaxTotal = inTaxTotal.add(expenseLine.getTotalAmount());
        companyExTaxTotal = companyExTaxTotal.add(expenseLine.getCompanyUntaxedAmount());
        companyTaxTotal = companyTaxTotal.add(expenseLine.getCompanyTotalTax());
        companyInTaxTotal = companyInTaxTotal.add(expenseLine.getCompanyTotalAmount());
      }
    }
    if (ObjectUtils.notEmpty(kilometricExpenseLineList)) {
      for (ExpenseLine kilometricExpenseLine : kilometricExpenseLineList) {
        if (kilometricExpenseLine.getUntaxedAmount() != null) {
          exTaxTotal = exTaxTotal.add(kilometricExpenseLine.getUntaxedAmount());
          companyExTaxTotal =
              companyExTaxTotal.add(kilometricExpenseLine.getCompanyUntaxedAmount());
        }
        if (kilometricExpenseLine.getTotalTax() != null) {
          taxTotal = taxTotal.add(kilometricExpenseLine.getTotalTax());
          companyTaxTotal = companyTaxTotal.add(kilometricExpenseLine.getCompanyTotalTax());
        }
        if (kilometricExpenseLine.getTotalAmount() != null) {
          inTaxTotal = inTaxTotal.add(kilometricExpenseLine.getTotalAmount());
          companyInTaxTotal = companyInTaxTotal.add(kilometricExpenseLine.getCompanyTotalAmount());
        }
      }
    }
    expense.setExTaxTotal(exTaxTotal);
    expense.setTaxTotal(taxTotal);
    expense.setInTaxTotal(inTaxTotal);
    expense.setCompanyExTaxTotal(companyExTaxTotal);
    expense.setCompanyTaxTotal(companyTaxTotal);
    expense.setCompanyInTaxTotal(companyInTaxTotal);
    return expense;
  }

  @Override
  public BigDecimal computePersonalExpenseAmount(Expense expense) {

    BigDecimal personalExpenseAmount = new BigDecimal("0.00");

    for (ExpenseLine expenseLine : expenseLineService.getExpenseLineList(expense)) {
      if (expenseLine.getExpenseProduct() != null
          && expenseLine.getExpenseProduct().getPersonalExpense()) {
        personalExpenseAmount = personalExpenseAmount.add(expenseLine.getTotalAmount());
      }
    }
    return personalExpenseAmount;
  }

  @Override
  public BigDecimal computeAdvanceAmount(Expense expense) throws AxelorException {

    BigDecimal advanceAmount = new BigDecimal("0.00");

    if (expense.getEmployeeAdvanceUsageList() != null
        && !expense.getEmployeeAdvanceUsageList().isEmpty()) {
      for (EmployeeAdvanceUsage advanceLine : expense.getEmployeeAdvanceUsageList()) {
        advanceAmount = advanceAmount.add(advanceLine.getUsedAmount());
      }
    }

    Currency companyCurrency =
        Optional.of(expense).map(Expense::getCompany).map(Company::getCurrency).orElse(null);

    return currencyService.getAmountCurrencyConvertedAtDate(
        companyCurrency, expense.getCurrency(), advanceAmount, expense.getPaymentDate());
  }
}
