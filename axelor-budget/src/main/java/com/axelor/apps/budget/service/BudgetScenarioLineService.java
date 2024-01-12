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
package com.axelor.apps.budget.service;

import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetScenario;
import com.axelor.apps.budget.db.BudgetScenarioLine;
import com.axelor.apps.budget.db.BudgetScenarioVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BudgetScenarioLineService {
  ArrayList<Integer> getFiscalYears(BudgetScenario budgetScenario);

  void removeUnusedYears(BudgetScenarioLine budgetScenarioLine, int size);

  List<Map<String, Object>> getLineUsingSection(
      BudgetLevel section,
      Set<BudgetScenarioVariable> budgetScenarioVariableSet,
      List<BudgetScenarioLine> budgetScenarioLineOriginList,
      List<Map<String, Object>> budgetScenarioLineList);
}
