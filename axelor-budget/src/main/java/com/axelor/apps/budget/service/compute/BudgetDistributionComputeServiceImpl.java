/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.service.compute;

import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.common.ObjectUtils;
import com.axelor.studio.db.AppBudget;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class BudgetDistributionComputeServiceImpl implements BudgetDistributionComputeService {

  protected AppBudgetService appBudgetService;

  @Inject
  public BudgetDistributionComputeServiceImpl(AppBudgetService appBudgetService) {
    this.appBudgetService = appBudgetService;
  }

  @Override
  public void updateMonoBudgetAmounts(
      List<BudgetDistribution> budgetDistributionList, BigDecimal newAmount) {
    if (Optional.ofNullable(appBudgetService.getAppBudget())
            .map(AppBudget::getManageMultiBudget)
            .orElse(true)
        || ObjectUtils.isEmpty(budgetDistributionList)
        || budgetDistributionList.size() != 1) {
      return;
    }

    BudgetDistribution budgetDistribution = budgetDistributionList.get(0);
    budgetDistribution.setAmount(newAmount);
  }
}
