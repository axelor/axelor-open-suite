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

import com.axelor.apps.hr.db.EmployeeAdvanceUsage;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Singleton
public class ExpenseComputationServiceImpl implements ExpenseComputationService {

  protected ExpenseLineService expenseLineService;

  @Inject
  public ExpenseComputationServiceImpl(ExpenseLineService expenseLineService) {
    this.expenseLineService = expenseLineService;
  }

  @Override
  public Expense compute(Expense expense) {

    BigDecimal exTaxTotal = BigDecimal.ZERO;
    BigDecimal taxTotal = BigDecimal.ZERO;
    BigDecimal inTaxTotal = BigDecimal.ZERO;
    BigDecimal chargedFeeAmount = BigDecimal.ZERO;
    BigDecimal totalToInvoice = BigDecimal.ZERO;
    List<ExpenseLine> generalExpenseLineList = expense.getGeneralExpenseLineList();
    List<ExpenseLine> kilometricExpenseLineList = expense.getKilometricExpenseLineList();

    if (generalExpenseLineList != null) {
      for (ExpenseLine expenseLine : generalExpenseLineList) {
        exTaxTotal = exTaxTotal.add(expenseLine.getUntaxedAmount());
        taxTotal = taxTotal.add(expenseLine.getTotalTax());
        inTaxTotal = inTaxTotal.add(expenseLine.getTotalAmount());
        chargedFeeAmount = chargedFeeAmount.add(expenseLine.getChargedFeeAmount());
        totalToInvoice = totalToInvoice.add(expenseLine.getTotalAmountToInvoice());
      }
    }
    if (kilometricExpenseLineList != null) {
      for (ExpenseLine kilometricExpenseLine : kilometricExpenseLineList) {
        if (kilometricExpenseLine.getUntaxedAmount() != null) {
          exTaxTotal = exTaxTotal.add(kilometricExpenseLine.getUntaxedAmount());
        }
        if (kilometricExpenseLine.getTotalTax() != null) {
          taxTotal = taxTotal.add(kilometricExpenseLine.getTotalTax());
        }
        if (kilometricExpenseLine.getTotalAmount() != null) {
          inTaxTotal = inTaxTotal.add(kilometricExpenseLine.getTotalAmount());
        }
      }
    }
    expense.setExTaxTotal(exTaxTotal);
    expense.setTaxTotal(taxTotal);
    expense.setInTaxTotal(inTaxTotal);
    expense.setChargedFeeAmount(chargedFeeAmount);
    expense.setTotalAmountToInvoice(totalToInvoice);
    return expense;
  }

  @Override
  public void computeLine(ExpenseLine line) {
    line.setToInvoice(true);
    line.setEmployee(line.getExpense().getEmployee());
    line.setProject(line.getExpense().getProject());

    if (line.getCurrency() != null && line.getExpense() != null) {
      line.setExpense(line.getExpense());
    }

    if (line.getUntaxedAmount() != null) {
      BigDecimal untaxed = line.getUntaxedAmount();

      // Check if tax is not provided by the user
      if (line.getTotalTax() == null || line.getTotalTax().compareTo(BigDecimal.ZERO) == 0) {
        BigDecimal calculatedTax =
            untaxed.multiply(new BigDecimal("0.19")).setScale(2, RoundingMode.HALF_UP);
        line.setTotalTax(calculatedTax);
      }

      line.setTotalAmount(untaxed.add(line.getTotalTax()));
    }

    line.setTotalAmountToInvoice(line.getTotalAmount());
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
  public BigDecimal computeAdvanceAmount(Expense expense) {

    BigDecimal advanceAmount = new BigDecimal("0.00");

    if (expense.getEmployeeAdvanceUsageList() != null
        && !expense.getEmployeeAdvanceUsageList().isEmpty()) {
      for (EmployeeAdvanceUsage advanceLine : expense.getEmployeeAdvanceUsageList()) {
        advanceAmount = advanceAmount.add(advanceLine.getUsedAmount());
      }
    }

    return advanceAmount;
  }
}
