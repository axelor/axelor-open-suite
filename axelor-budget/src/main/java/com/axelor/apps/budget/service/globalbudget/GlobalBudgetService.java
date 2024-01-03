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
package com.axelor.apps.budget.service.globalbudget;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetVersion;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.GlobalBudgetTemplate;

public interface GlobalBudgetService {
  void validateDates(GlobalBudget globalBudget) throws AxelorException;

  void computeBudgetLevelTotals(Budget budget);

  void computeTotals(GlobalBudget globalBudget);

  GlobalBudget generateGlobalBudgetWithTemplate(GlobalBudgetTemplate globalBudgetTemplate)
      throws AxelorException;

  GlobalBudget changeBudgetVersion(
      GlobalBudget globalBudget, BudgetVersion budgetVersion, boolean needRecomputeBudgetLine)
      throws AxelorException;

  void updateGlobalBudgetDates(GlobalBudget globalBudget) throws AxelorException;

  void generateBudgetKey(GlobalBudget globalBudget) throws AxelorException;
}
