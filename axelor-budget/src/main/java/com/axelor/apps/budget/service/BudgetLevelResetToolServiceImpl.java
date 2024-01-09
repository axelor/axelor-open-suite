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
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.math.BigDecimal;
import org.apache.commons.collections.CollectionUtils;

public class BudgetLevelResetToolServiceImpl implements BudgetLevelResetToolService {

  protected BudgetResetToolService budgetResetToolService;

  @Inject
  public BudgetLevelResetToolServiceImpl(BudgetResetToolService budgetResetToolService) {
    this.budgetResetToolService = budgetResetToolService;
  }

  @Override
  public void resetBudgetLevel(BudgetLevel budgetLevel) {

    budgetLevel.setStatusSelect(BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_DRAFT);

    budgetLevel.setArchived(false);

    budgetLevel.setTotalAmountCommitted(BigDecimal.ZERO);
    budgetLevel.setTotalAmountAvailable(budgetLevel.getTotalAmountExpected());
    budgetLevel.setAvailableAmountWithSimulated(budgetLevel.getTotalAmountExpected());
    budgetLevel.setRealizedWithNoPo(BigDecimal.ZERO);
    budgetLevel.setRealizedWithPo(BigDecimal.ZERO);
    budgetLevel.setSimulatedAmount(BigDecimal.ZERO);
    budgetLevel.setTotalFirmGap(BigDecimal.ZERO);
    budgetLevel.setTotalAmountPaid(BigDecimal.ZERO);

    if (!ObjectUtils.isEmpty(budgetLevel.getBudgetLevelList())) {
      budgetLevel.getBudgetLevelList().forEach(child -> resetBudgetLevel(child));
    } else if (!CollectionUtils.isEmpty(budgetLevel.getBudgetList())) {
      budgetLevel.getBudgetList().forEach(budgetResetToolService::resetBudget);
    }
  }
}
