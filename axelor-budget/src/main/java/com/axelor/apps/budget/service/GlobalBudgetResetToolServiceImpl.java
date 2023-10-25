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
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

public class GlobalBudgetResetToolServiceImpl implements GlobalBudgetResetToolService {

  private final BudgetLevelResetToolService budgetLevelResetToolService;

  @Inject
  public GlobalBudgetResetToolServiceImpl(BudgetLevelResetToolService budgetLevelResetToolService) {
    this.budgetLevelResetToolService = budgetLevelResetToolService;
  }

  public void resetGlobalBudget(GlobalBudget globalBudget) {
    globalBudget.setCode(globalBudget.getCode() + " (" + I18n.get("copy") + ")");
    globalBudget.setStatusSelect(GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_DRAFT);
    globalBudget.setArchived(false);

    globalBudget.setTotalAmountCommitted(BigDecimal.ZERO);
    globalBudget.setTotalAmountAvailable(globalBudget.getTotalAmountExpected());
    globalBudget.setAvailableAmountWithSimulated(globalBudget.getTotalAmountExpected());
    globalBudget.setRealizedWithNoPo(BigDecimal.ZERO);
    globalBudget.setRealizedWithPo(BigDecimal.ZERO);
    globalBudget.setSimulatedAmount(BigDecimal.ZERO);
    globalBudget.setTotalFirmGap(BigDecimal.ZERO);
    globalBudget.setTotalAmountPaid(BigDecimal.ZERO);
    globalBudget.setActiveVersion(null);
    globalBudget.clearBudgetVersionList();
    globalBudget.clearBudgetList();
    List<BudgetLevel> budgetLevels = globalBudget.getBudgetLevelList();

    if (ObjectUtils.notEmpty(budgetLevels)) {
      budgetLevels.forEach(budgetLevelResetToolService::resetBudgetLevel);
    }
  }
}
