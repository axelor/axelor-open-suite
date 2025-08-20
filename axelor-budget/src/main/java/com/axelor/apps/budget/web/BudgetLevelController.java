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
package com.axelor.apps.budget.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.apps.budget.service.BudgetComputeHiddenDateService;
import com.axelor.apps.budget.service.BudgetLevelService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.time.LocalDate;

public class BudgetLevelController {

  @SuppressWarnings("unchecked")
  public void setDates(ActionRequest request, ActionResponse response) throws AxelorException {
    BudgetLevel budgetLevel = request.getContext().asType(BudgetLevel.class);
    LocalDate fromDate = budgetLevel.getFromDate();
    LocalDate toDate = budgetLevel.getToDate();
    BudgetLevelService budgetLevelService = Beans.get(BudgetLevelService.class);

    if (!ObjectUtils.isEmpty(budgetLevel.getBudgetLevelList())) {
      budgetLevelService.getUpdatedBudgetLevelList(
          budgetLevel.getBudgetLevelList(), fromDate, toDate);
      response.setReload(true);
    } else if (!ObjectUtils.isEmpty(budgetLevel.getBudgetList())) {
      budgetLevelService.getUpdatedBudgetList(budgetLevel.getBudgetList(), fromDate, toDate);
      response.setReload(true);
    }
  }

  @ErrorException
  public void validate(ActionRequest request, ActionResponse response) throws AxelorException {
    BudgetLevel budgetLevel = request.getContext().asType(BudgetLevel.class);
    Beans.get(BudgetLevelService.class).validateChildren(budgetLevel);
    response.setReload(true);
  }

  @ErrorException
  public void draft(ActionRequest request, ActionResponse response) throws AxelorException {
    BudgetLevel budgetLevel = request.getContext().asType(BudgetLevel.class);
    Beans.get(BudgetLevelService.class).draftChildren(budgetLevel);
    response.setReload(true);
  }

  public void showButtonFields(ActionRequest request, ActionResponse response) {
    BudgetLevel budgetLevel = request.getContext().asType(BudgetLevel.class);
    GlobalBudget globalBudget =
        Beans.get(BudgetToolsService.class).getGlobalBudgetUsingBudgetLevel(budgetLevel);
    if (globalBudget != null
        && globalBudget.getStatusSelect()
            != GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_VALID_STRUCTURE) {
      return;
    }
    response.setAttr("buttonsPanel", "hidden", false);
    if (BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_DRAFT.equals(
        budgetLevel.getStatusSelect())) {
      response.setAttr("validateBtn", "hidden", false);
    } else {
      response.setAttr("draftBtn", "hidden", false);
    }
  }

  @ErrorException
  public void showUpdateDatesBtn(ActionRequest request, ActionResponse response) {
    BudgetLevel budgetLevel = request.getContext().asType(BudgetLevel.class);
    boolean isHidden = Beans.get(BudgetComputeHiddenDateService.class).isHidden(budgetLevel);
    response.setAttr("updateDatesBtn", "hidden", isHidden);
  }

  public void computeLevelAmounts(ActionRequest request, ActionResponse response) {
    BudgetLevel budgetLevel = request.getContext().asType(BudgetLevel.class);
    Beans.get(BudgetLevelService.class).computeTotals(budgetLevel);
    response.setValue("totalAmountExpected", budgetLevel.getTotalAmountExpected());
    response.setValue("totalAmountAvailable", budgetLevel.getTotalAmountAvailable());
  }
}
