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
import com.axelor.apps.budget.service.CurrencyScaleServiceBudget;
import com.axelor.apps.budget.service.GlobalBudgetService;
import com.axelor.inject.Beans;
import java.math.BigDecimal;

public class BudgetManagementRepository extends BudgetRepository {

  @Override
  public Budget save(Budget entity) {
    CurrencyScaleServiceBudget currencyScaleServiceBudget =
        Beans.get(CurrencyScaleServiceBudget.class);

    entity.setAvailableAmount(
        currencyScaleServiceBudget.getCompanyScaledValue(
            entity,
            (entity
                    .getTotalAmountExpected()
                    .subtract(entity.getRealizedWithPo())
                    .subtract(entity.getRealizedWithNoPo()))
                .max(BigDecimal.ZERO)));

    entity.setAvailableAmountWithSimulated(
        currencyScaleServiceBudget.getCompanyScaledValue(
            entity,
            entity
                .getAvailableAmount()
                .subtract(entity.getSimulatedAmount())
                .max(BigDecimal.ZERO)));

    GlobalBudgetService globalBudgetService = Beans.get(GlobalBudgetService.class);
    globalBudgetService.computeBudgetLevelTotals(entity);

    entity = super.save(entity);

    return entity;
  }
}
