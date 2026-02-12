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
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.KilometricAllowParam;
import com.axelor.auth.db.User;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import java.util.List;
import java.util.Map;

public interface ExpenseViewService {

  ActionViewBuilder buildEditExpenseView(User user);

  ActionViewBuilder buildEditSelectedExpenseView(Long expenseId);

  ActionViewBuilder buildHistoricExpenseView(User user, Employee employee);

  ActionViewBuilder buildSubordinateExpensesView(User user, Company activeCompany);

  void setExpense(ActionRequest request, ExpenseLine expenseLine);

  List<KilometricAllowParam> domainOnSelectOnKAP(
      ExpenseLine expenseLine, Map<String, Map<String, Object>> attrsMap) throws AxelorException;
}
