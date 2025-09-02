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

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.date.BudgetInitDateService;
import com.axelor.apps.budget.service.move.MoveBudgetService;
import com.axelor.apps.budget.web.tool.BudgetControllerTool;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import org.apache.commons.collections.CollectionUtils;

public class MoveController {

  public void computeBudgetDistribution(ActionRequest request, ActionResponse response) {

    try {
      Move move = request.getContext().asType(Move.class);
      MoveBudgetService moveBudgetService = Beans.get(MoveBudgetService.class);
      BudgetToolsService budgetToolsService = Beans.get(BudgetToolsService.class);
      if (move != null
          && move.getCompany() != null
          && budgetToolsService.checkBudgetKeyInConfig(move.getCompany())) {
        if (!budgetToolsService.checkBudgetKeyAndRole(move.getCompany(), AuthUtils.getUser())
            && moveBudgetService.isBudgetInLines(move)) {
          response.setInfo(
              I18n.get(
                  BudgetExceptionMessage.BUDGET_ROLE_NOT_IN_BUDGET_DISTRIBUTION_AUTHORIZED_LIST));
          return;
        }
        String alertMessage = moveBudgetService.computeBudgetDistribution(move);
        move.setBudgetDistributionGenerated(moveBudgetService.isBudgetInLines(move));

        response.setValues(move);

        if (!Strings.isNullOrEmpty(alertMessage)) {
          response.setInfo(
              String.format(I18n.get(BudgetExceptionMessage.BUDGET_KEY_NOT_FOUND), alertMessage));
        }
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @ErrorException
  public void autoComputeBudgetDistribution(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Move move = request.getContext().asType(Move.class);
    MoveBudgetService moveBudgetService = Beans.get(MoveBudgetService.class);
    if (move != null
        && !CollectionUtils.isEmpty(move.getMoveLineList())
        && !moveBudgetService.isBudgetInLines(move)) {
      moveBudgetService.autoComputeBudgetDistribution(move);
      response.setValue("moveLineList", move.getMoveLineList());
    }
  }

  public void computeMoveBudgetRemainingAmountToAllocate(
      ActionRequest request, ActionResponse response) {
    try {
      Move move = request.getContext().asType(Move.class);
      if (move != null && !CollectionUtils.isEmpty(move.getMoveLineList())) {
        BudgetToolsService budgetToolsService = Beans.get(BudgetToolsService.class);
        for (MoveLine moveLine : move.getMoveLineList()) {
          moveLine.setBudgetRemainingAmountToAllocate(
              budgetToolsService.getBudgetRemainingAmountToAllocate(
                  moveLine.getBudgetDistributionList(),
                  moveLine.getDebit().max(moveLine.getCredit())));
        }
        response.setValue("moveLineList", move.getMoveLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validateAccounting(ActionRequest request, ActionResponse response) {
    Move move = request.getContext().asType(Move.class);
    MoveBudgetService moveBudgetService = Beans.get(MoveBudgetService.class);
    if (move != null && CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      if (moveBudgetService.isBudgetInLines(move)) {
        String budgetExceedAlert = moveBudgetService.getBudgetExceedAlert(move);
        BudgetControllerTool.verifyBudgetExceed(budgetExceedAlert, false, response);
      } else if (ObjectUtils.notEmpty(moveBudgetService.getRequiredBudgetMoveLines(move))) {
        BudgetControllerTool.verifyMissingBudget(response);
      }
    }
  }

  public void initializeBudgetDates(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Move move = request.getContext().asType(Move.class);
    Beans.get(BudgetInitDateService.class).initializeBudgetDates(move);

    response.setValue("moveLineList", move.getMoveLineList());
  }
}
