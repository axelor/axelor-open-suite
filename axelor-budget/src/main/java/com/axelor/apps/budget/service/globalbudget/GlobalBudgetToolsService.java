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
package com.axelor.apps.budget.service.globalbudget;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.GlobalBudget;
import java.util.List;
import java.util.Map;

public interface GlobalBudgetToolsService {
  List<Budget> getAllBudgets(GlobalBudget globalBudget);

  List<Budget> getAllBudgets(BudgetLevel budgetLevel, List<Budget> budgetList);

  List<Long> getAllBudgetIds(GlobalBudget globalBudget);

  void fillGlobalBudgetOnBudget(GlobalBudget globalBudget);

  Map<String, Map<String, Object>> manageHiddenAmounts(boolean hidden);
}
