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
package com.axelor.apps.budget.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetScenario;
import com.axelor.apps.budget.db.GlobalBudget;
import java.math.BigDecimal;

public interface CurrencyScaleServiceBudget {

  BigDecimal getCompanyScaledValue(BudgetScenario budgetScenario, BigDecimal amount);

  BigDecimal getCompanyScaledValue(Budget budget, BigDecimal amount);

  BigDecimal getCompanyScaledValue(BudgetLevel budgetLevel, BigDecimal amount);

  BigDecimal getCompanyScaledValue(GlobalBudget globalBudget, BigDecimal amount);

  BigDecimal getCompanyScaledValue(BudgetDistribution budgetDistribution, BigDecimal amount);

  int getCompanyScale(BudgetScenario budgetScenario);

  int getCompanyScale(Budget budget);

  int getCompanyScale(BudgetLevel budgetLevel);

  int getCompanyScale(GlobalBudget globalBudget);

  int getCompanyScale(BudgetDistribution budgetDistribution);

  int getCompanyScale(Company company);
}
