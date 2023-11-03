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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.google.inject.Inject;

public class GlobalBudgetGroupServiceImpl implements GlobalBudgetGroupService {

  protected GlobalBudgetService globalBudgetService;
  protected GlobalBudgetWorkflowService globalBudgetWorkflowService;

  @Inject
  public void GlobalBudgetGroupServiceImpl(
      GlobalBudgetService globalBudgetService,
      GlobalBudgetWorkflowService globalBudgetWorkflowService) {
    this.globalBudgetService = globalBudgetService;
    this.globalBudgetWorkflowService = globalBudgetWorkflowService;
  }

  @Override
  public void validateStructure(GlobalBudget globalBudget) throws AxelorException {

    globalBudgetWorkflowService.validateChildren(
        globalBudget, GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_VALID_STRUCTURE);
  }
}
