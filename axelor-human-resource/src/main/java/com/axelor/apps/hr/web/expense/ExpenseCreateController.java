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
package com.axelor.apps.hr.web.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.expense.ExpenseCreateWizardService;
import com.axelor.apps.hr.service.expense.ExpenseToolService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.MapHelper;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ExpenseCreateController {

  public void createExpense(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    List<ExpenseLine> expenseLineToMerge =
        MapHelper.getCollection(context, ExpenseLine.class, "expenseLinesToMerge");
    if (CollectionUtils.isEmpty(expenseLineToMerge)) {
      response.setError(I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_NO_LINE_SELECTED));
    } else {
      Expense expense =
          Beans.get(ExpenseCreateWizardService.class).createExpense(expenseLineToMerge);

      openExpense(response, expense);
    }
  }

  public void addToExpense(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    List<ExpenseLine> expenseLineToMerge =
        MapHelper.getCollection(context, ExpenseLine.class, "expenseLinesToMerge");
    if (CollectionUtils.isEmpty(expenseLineToMerge)) {
      response.setError(I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_NO_LINE_SELECTED));
    } else {
      Expense expense = MapHelper.get(context, Expense.class, "expense");

      Beans.get(ExpenseToolService.class)
          .addExpenseLinesToExpenseAndCompute(expense, expenseLineToMerge);

      openExpense(response, expense);
    }
  }

  protected void openExpense(ActionResponse response, Expense expense) {
    if (expense != null) {
      response.setView(
          ActionView.define(I18n.get("Expense"))
              .model(Expense.class.getName())
              .add("grid", "expense-grid")
              .add("form", "expense-form")
              .param("search-filters", "expense-filters")
              .param("forceEdit", "true")
              .context("_showRecord", String.valueOf(expense.getId()))
              .map());
      response.setCanClose(true);
    }
  }

  public void fillList(ActionRequest request, ActionResponse response) {
    List<Integer> idList = (List<Integer>) request.getContext().get("_selectedLines");
    if (idList != null) {
      List<ExpenseLine> expenseLineList =
          Beans.get(ExpenseLineRepository.class)
              .findByIds(idList.stream().map(Integer::longValue).collect(Collectors.toList()));
      response.setValue("$expenseLinesToMerge", expenseLineList);
    }
  }

  public void getExpenseDomain(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    List<ExpenseLine> expenseLineToMerge =
        MapHelper.getCollection(context, ExpenseLine.class, "expenseLinesToMerge");
    response.setAttr(
        "expense",
        "domain",
        Beans.get(ExpenseCreateWizardService.class).getExpenseDomain(expenseLineToMerge));
  }
}
