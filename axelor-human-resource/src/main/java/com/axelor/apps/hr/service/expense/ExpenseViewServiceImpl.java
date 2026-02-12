/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.KilometricAllowParam;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.auth.db.User;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.Context;
import com.axelor.utils.db.Wizard;
import com.axelor.utils.helpers.StringHelper;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ExpenseViewServiceImpl implements ExpenseViewService {

  protected ExpenseRepository expenseRepository;
  protected UserHrService userHrService;
  protected ExpenseKilometricService expenseKilometricService;

  @Inject
  public ExpenseViewServiceImpl(
      ExpenseRepository expenseRepository,
      UserHrService userHrService,
      ExpenseKilometricService expenseKilometricService) {
    this.expenseRepository = expenseRepository;
    this.userHrService = userHrService;
    this.expenseKilometricService = expenseKilometricService;
  }

  @Override
  public ActionViewBuilder buildEditExpenseView(User user) {

    List<Expense> expenseList =
        expenseRepository
            .all()
            .filter(
                "self.employee.user.id = ?1 AND self.company = ?2 AND self.statusSelect = 1",
                user.getId(),
                Optional.ofNullable(user).map(User::getActiveCompany).orElse(null))
            .fetch();
    String expenseTitle = I18n.get("Expense");

    if (expenseList.isEmpty()) {
      return ActionView.define(expenseTitle)
          .model(Expense.class.getName())
          .add("form", "complete-my-expense-form")
          .context("_payCompany", userHrService.getPayCompany(user))
          .context("_isEmployeeReadOnly", true);
    } else if (expenseList.size() == 1) {
      return ActionView.define(expenseTitle)
          .model(Expense.class.getName())
          .add("form", "complete-my-expense-form")
          .param("forceEdit", "true")
          .context("_showRecord", String.valueOf(expenseList.get(0).getId()))
          .context("_isEmployeeReadOnly", true);
    } else {
      return ActionView.define(expenseTitle)
          .model(Wizard.class.getName())
          .add("form", "popup-expense-form")
          .param("forceEdit", "true")
          .param("popup", "true")
          .param("show-toolbar", "false")
          .param("show-confirm", "false")
          .param("forceEdit", "true")
          .param("popup-save", "false");
    }
  }

  @Override
  public ActionViewBuilder buildEditSelectedExpenseView(Long expenseId) {
    return ActionView.define(I18n.get("Expense"))
        .model(Expense.class.getName())
        .add("form", "complete-my-expense-form")
        .param("forceEdit", "true")
        .domain("self.id = " + expenseId)
        .context("_showRecord", expenseId)
        .context("_isEmployeeReadOnly", true);
  }

  @Override
  public ActionViewBuilder buildHistoricExpenseView(User user, Employee employee) {
    ActionViewBuilder actionView =
        ActionView.define(I18n.get("Historic colleague Expenses"))
            .model(Expense.class.getName())
            .add("grid", "expense-grid")
            .add("form", "expense-form")
            .param("search-filters", "expense-filters")
            .domain(
                "self.company = :_activeCompany AND (self.statusSelect = 3 OR self.statusSelect = 4)")
            .context("_activeCompany", user.getActiveCompany());

    if (employee == null || !employee.getHrManager()) {
      actionView
          .domain(actionView.get().getDomain() + " AND self.employee.managerUser = :_user")
          .context("_user", user);
    }
    return actionView;
  }

  @Override
  public ActionViewBuilder buildSubordinateExpensesView(User user, Company activeCompany) {
    String domain =
        "self.employee.managerUser.employee.managerUser = :_user "
            + "AND self.company = :_activeCompany "
            + "AND self.statusSelect = 2";

    long nbExpenses =
        Query.of(Expense.class)
            .filter(domain)
            .bind("_user", user)
            .bind("_activeCompany", activeCompany)
            .count();
    if (nbExpenses == 0) {
      return null;
    }

    return ActionView.define(I18n.get("Expenses to be Validated by your subordinates"))
        .model(Expense.class.getName())
        .add("grid", "expense-grid")
        .add("form", "expense-form")
        .param("search-filters", "expense-filters")
        .domain(domain)
        .context("_user", user)
        .context("_activeCompany", activeCompany);
  }

  @Override
  public void setExpense(ActionRequest request, ExpenseLine expenseLine) {
    if (expenseLine.getExpense() != null) {
      return;
    }
    Context parent = request.getContext().getParent();
    if (parent != null && parent.get("_model").equals(Expense.class.getName())) {
      expenseLine.setExpense(parent.asType(Expense.class));
    }
  }

  @Override
  public List<KilometricAllowParam> domainOnSelectOnKAP(
      ExpenseLine expenseLine, Map<String, Map<String, Object>> attrsMap) throws AxelorException {
    List<KilometricAllowParam> kilometricAllowParamList =
        expenseKilometricService.getListOfKilometricAllowParamVehicleFilter(expenseLine);

    this.addAttr(
        "kilometricAllowParam",
        "domain",
        "self.id IN (" + StringHelper.getIdListString(kilometricAllowParamList) + ")",
        attrsMap);
    return kilometricAllowParamList;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }
}
