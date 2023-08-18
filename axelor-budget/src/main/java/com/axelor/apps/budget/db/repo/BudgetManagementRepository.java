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
package com.axelor.apps.budget.db.repo;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.service.BudgetLevelService;
import com.axelor.inject.Beans;
import java.math.BigDecimal;

public class BudgetManagementRepository extends BudgetRepository {

  @Override
  public Budget save(Budget entity) {
    entity.setAvailableAmount(
        (entity
                        .getBudgetVersion()
                        .getTotalAmountExpected()
                        .subtract(entity.getGlobalBudget().getRealizedWithPo())
                        .subtract(entity.getGlobalBudget().getRealizedWithNoPo()))
                    .compareTo(BigDecimal.ZERO)
                > 0
            ? entity
                .getBudgetVersion()
                .getTotalAmountExpected()
                .subtract(entity.getGlobalBudget().getRealizedWithPo())
                .subtract(entity.getGlobalBudget().getRealizedWithNoPo())
            : BigDecimal.ZERO);

    BudgetLevelService budgetLevelService = Beans.get(BudgetLevelService.class);

    entity = super.save(entity);

    budgetLevelService.computeBudgetLevelTotals(entity);

    return entity;
  }
}
