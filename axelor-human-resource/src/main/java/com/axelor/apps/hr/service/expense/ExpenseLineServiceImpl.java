/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class ExpenseLineServiceImpl implements ExpenseLineService {

  protected ExpenseLineRepository expenseLineRepository;
  protected AppHumanResourceService appHumanResourceService;
  protected ExpenseRepository expenseRepository;

  @Inject
  public ExpenseLineServiceImpl(
      ExpenseLineRepository expenseLineRepository,
      AppHumanResourceService appHumanResourceService,
      ExpenseRepository expenseRepository) {
    this.expenseLineRepository = expenseLineRepository;
    this.appHumanResourceService = appHumanResourceService;
    this.expenseRepository = expenseRepository;
  }

  @Override
  public List<ExpenseLine> getExpenseLineList(Expense expense) {
    List<ExpenseLine> expenseLineList = new ArrayList<>();
    if (expense.getGeneralExpenseLineList() != null) {
      expenseLineList.addAll(expense.getGeneralExpenseLineList());
    }
    if (expense.getKilometricExpenseLineList() != null) {
      expenseLineList.addAll(expense.getKilometricExpenseLineList());
    }
    return expenseLineList;
  }

  @Override
  public void completeExpenseLines(Expense expense) {
    List<ExpenseLine> expenseLineList =
        expenseLineRepository
            .all()
            .filter("self.expense.id = :_expenseId")
            .bind("_expenseId", expense.getId())
            .fetch();
    List<ExpenseLine> kilometricExpenseLineList = expense.getKilometricExpenseLineList();
    List<ExpenseLine> generalExpenseLineList = expense.getGeneralExpenseLineList();

    // removing expense from one O2M also remove the link
    for (ExpenseLine expenseLine : expenseLineList) {
      if (!kilometricExpenseLineList.contains(expenseLine)
          && !generalExpenseLineList.contains(expenseLine)) {
        expenseLine.setExpense(null);
        expenseLineRepository.remove(expenseLine);
      }
    }

    // adding expense in one O2M also add the link
    if (kilometricExpenseLineList != null) {
      for (ExpenseLine kilometricLine : kilometricExpenseLineList) {
        if (!expenseLineList.contains(kilometricLine)) {
          kilometricLine.setExpense(expense);
        }
      }
    }
    if (generalExpenseLineList != null) {
      for (ExpenseLine generalExpenseLine : generalExpenseLineList) {
        if (!expenseLineList.contains(generalExpenseLine)) {
          generalExpenseLine.setExpense(expense);
        }
      }
    }
  }

  @Override
  public boolean isTotalAmountGreaterThanExpenseLimit(Expense expense) {
    BigDecimal expenseLimit = appHumanResourceService.getAppExpense().getExpenseLimit();

    if (expenseLimit.compareTo(BigDecimal.ZERO) != 0) {
      BigDecimal totalInTax =
          findMatchingExpenses(expense).stream()
              .map(Expense::getInTaxTotal)
              .reduce(BigDecimal.ZERO, BigDecimal::add)
              .add(expense.getInTaxTotal());

      return totalInTax.compareTo(expenseLimit) > 0;
    }
    return false;
  }

  protected List<Expense> findMatchingExpenses(Expense expense) {
    return expenseRepository
        .all()
        .filter(
            "self.employee IS NOT NULL AND self.employee.id = :employeeId AND self.period IS NOT NULL AND self.period.id = :periodId AND self.id != :id")
        .bind("employeeId", expense.getEmployee().getId())
        .bind("periodId", expense.getPeriod().getId())
        .bind("id", expense.getId())
        .fetch();
  }
}
