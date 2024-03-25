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
package com.axelor.apps.budget.web.tool;

import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;

public class BudgetControllerTool {

  public static void verifyMissingBudget(ActionResponse response) {
    Boolean isError = Beans.get(AppBudgetService.class).isMissingBudgetCheckError();
    if (isError != null) {
      if (isError) {
        response.setError(I18n.get(BudgetExceptionMessage.NO_BUDGET_VALUES_FOUND_ERROR));
      } else {
        response.setAlert(I18n.get(BudgetExceptionMessage.NO_BUDGET_VALUES_FOUND));
      }
    }
  }

  public static void verifyBudgetExceed(
      String budgetExceedAlert, boolean isOrder, ActionResponse response) {
    if (!Strings.isNullOrEmpty(budgetExceedAlert)) {
      Boolean isError = Beans.get(AppBudgetService.class).isBudgetExceedValuesError(isOrder);
      if (isError != null) {
        budgetExceedAlert =
            Beans.get(BudgetToolsService.class)
                .getBudgetExceedMessage(budgetExceedAlert, isOrder, isError);
        if (isError) {
          response.setError(I18n.get(budgetExceedAlert));
        } else {
          response.setAlert(I18n.get(budgetExceedAlert));
        }
      }
    }
  }
}
