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
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.move.MoveBudgetDistributionService;
import com.axelor.apps.budget.service.move.MoveLineBudgetService;
import com.axelor.apps.budget.service.move.MoveLineToolBudgetService;
import com.axelor.db.EntityHelper;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Optional;

public class MoveLineController {

  public void validateBudgetLinesAmount(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Beans.get(MoveLineBudgetService.class).checkAmountForMoveLine(moveLine);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.INFORMATION);
    }
  }

  @ErrorException
  public void setBudgetDomain(ActionRequest request, ActionResponse response)
      throws AxelorException {
    MoveLine moveLine = request.getContext().asType(MoveLine.class);
    Move move = moveLine.getMove();
    if (move == null && request.getContext().getParent() != null) {
      move = request.getContext().getParent().asType(Move.class);
    }
    response.setAttr(
        "budget", "domain", Beans.get(MoveLineBudgetService.class).getBudgetDomain(move, moveLine));
  }

  public void computeBudgetRemainingAmountToAllocate(
      ActionRequest request, ActionResponse response) {
    MoveLine moveLine = request.getContext().asType(MoveLine.class);

    response.setValue(
        "budgetRemainingAmountToAllocate",
        Beans.get(BudgetToolsService.class)
            .getBudgetRemainingAmountToAllocate(
                moveLine.getBudgetDistributionList(),
                moveLine.getDebit().max(moveLine.getCredit())));
  }

  public void updateBudgetAmounts(ActionRequest request, ActionResponse response) {
    MoveLine moveLine = request.getContext().asType(MoveLine.class);
    moveLine = EntityHelper.getEntity(moveLine);
    boolean budgetAlreadyChanged =
        (Boolean)
            Optional.of(request.getContext()).map(c -> c.get("budgetAlreadyChanged")).orElse(false);

    Beans.get(MoveBudgetDistributionService.class).checkChanges(moveLine, budgetAlreadyChanged);
    response.setValue(
        "oldBudgetDistributionList",
        Beans.get(MoveLineToolBudgetService.class).copyBudgetDistributionList(moveLine));
    response.setValue("$budgetAlreadyChanged", true);
  }

  public void changeBudgetDistribution(ActionRequest request, ActionResponse response) {
    MoveLine moveLine = request.getContext().asType(MoveLine.class);

    Beans.get(MoveLineBudgetService.class).changeBudgetDistribution(moveLine);
    response.setValue("budgetDistributionList", moveLine.getBudgetDistributionList());
    response.setValue("oldBudgetDistributionList", moveLine.getOldBudgetDistributionList());
    response.setValue(
        "budgetRemainingAmountToAllocate", moveLine.getBudgetRemainingAmountToAllocate());
    response.setValue("$budgetAlreadyChanged", true);
  }
}
