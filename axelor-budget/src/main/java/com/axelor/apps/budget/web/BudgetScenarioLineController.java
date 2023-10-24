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
package com.axelor.apps.budget.web;

import com.axelor.apps.budget.db.BudgetScenario;
import com.axelor.apps.budget.service.BudgetScenarioLineService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.ArrayList;

public class BudgetScenarioLineController {
  public void changeColumnsNames(ActionRequest request, ActionResponse response) {
    BudgetScenario budgetScenario = request.getContext().getParent().asType(BudgetScenario.class);
    ArrayList<Integer> yearsList =
        Beans.get(BudgetScenarioLineService.class).getFiscalYears(budgetScenario);

    for (int i = 0; i < Math.min(yearsList.size(), 6); i++) {
      if (yearsList.contains(yearsList.get(i))) {
        String fieldName = "year" + (i + 1) + "Value";
        response.setAttr(fieldName, "hidden", false);
        response.setAttr(fieldName, "title", Integer.toString(yearsList.get(i)));
      }
    }
  }
}
