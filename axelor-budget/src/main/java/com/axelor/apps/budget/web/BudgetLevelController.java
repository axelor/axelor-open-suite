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
package com.axelor.apps.budget.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.service.BudgetLevelService;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.time.LocalDate;

public class BudgetLevelController {

  @SuppressWarnings("unchecked")
  public void setDates(ActionRequest request, ActionResponse response) throws AxelorException {
    BudgetLevel budgetLevel = request.getContext().asType(BudgetLevel.class);
    LocalDate fromDate = budgetLevel.getFromDate();
    LocalDate toDate = budgetLevel.getToDate();
    BudgetLevelService budgetLevelService = Beans.get(BudgetLevelService.class);

    if (!ObjectUtils.isEmpty(budgetLevel.getBudgetLevelList())) {
      budgetLevelService.getUpdatedBudgetLevelList(
          budgetLevel.getBudgetLevelList(), fromDate, toDate);
      response.setReload(true);
    } else if (!ObjectUtils.isEmpty(budgetLevel.getBudgetList())) {
      budgetLevelService.getUpdatedBudgetList(budgetLevel.getBudgetList(), fromDate, toDate);
      response.setReload(true);
    }
  }
}
