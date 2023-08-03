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
package com.axelor.apps.budget.web;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.move.MoveBudgetService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;

public class MoveController {

  public void computeBudgetDistribution(ActionRequest request, ActionResponse response) {

    try {
      Move move = request.getContext().asType(Move.class);
      MoveBudgetService moveBudgetService = Beans.get(MoveBudgetService.class);
      if (move != null
          && move.getCompany() != null
          && Beans.get(BudgetService.class).checkBudgetKeyInConfig(move.getCompany())) {
        if (!Beans.get(BudgetToolsService.class)
                .checkBudgetKeyAndRole(move.getCompany(), AuthUtils.getUser())
            && moveBudgetService.isBudgetInLines(move)) {
          response.setInfo(
              I18n.get(
                  BudgetExceptionMessage.BUDGET_ROLE_NOT_IN_BUDGET_DISTRIBUTION_AUTHORIZED_LIST));
          return;
        }
        String alertMessage = moveBudgetService.computeBudgetDistribution(move);

        response.setValue("budgetDistributionGenerated", moveBudgetService.isBudgetInLines(move));
        response.setValue("moveLineList", move.getMoveLineList());

        if (!Strings.isNullOrEmpty(alertMessage)) {
          response.setInfo(
              String.format(I18n.get(BudgetExceptionMessage.BUDGET_KEY_NOT_FOUND), alertMessage));
        }
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkBudgetDistribution(ActionRequest request, ActionResponse response) {

    try {
      Move move = request.getContext().asType(Move.class);
      MoveBudgetService moveBudgetService = Beans.get(MoveBudgetService.class);
      if (moveBudgetService.checkMissingBudgetDistributionOnAccountedMove(move)) {
        response.setAlert(I18n.get(BudgetExceptionMessage.NO_BUDGET_DISTRIBUTION_GENERATED));
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validateBudgetBalance(ActionRequest request, ActionResponse response) {

    try {
      Move move = request.getContext().asType(Move.class);

      Beans.get(MoveBudgetService.class).getBudgetExceedAlert(move);

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.WARNING);
    }
  }
}
