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
package com.axelor.apps.budget.db.repo;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetResetToolService;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetToolsService;
import com.google.inject.Inject;

public class GlobalBudgetManagementRepository extends GlobalBudgetRepository {

  protected AppBaseService appBaseService;
  protected GlobalBudgetResetToolService globalBudgetResetToolService;
  protected GlobalBudgetToolsService globalBudgetToolsService;

  @Inject
  public GlobalBudgetManagementRepository(
      AppBaseService appBaseService,
      GlobalBudgetResetToolService globalBudgetResetToolService,
      GlobalBudgetToolsService globalBudgetToolsService) {
    this.appBaseService = appBaseService;
    this.globalBudgetResetToolService = globalBudgetResetToolService;
    this.globalBudgetToolsService = globalBudgetToolsService;
  }

  @Override
  public GlobalBudget copy(GlobalBudget entity, boolean deep) {
    GlobalBudget copy = super.copy(entity, deep);

    if (appBaseService.isApp("budget")) {
      globalBudgetResetToolService.resetGlobalBudget(copy);
    }
    return copy;
  }

  @Override
  public GlobalBudget save(GlobalBudget entity) {
    globalBudgetToolsService.fillGlobalBudgetOnBudget(entity);
    entity = super.save(entity);
    return entity;
  }
}
