package com.axelor.apps.budget.web;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.move.MoveLineBudgetService;
import com.axelor.auth.AuthUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class MoveLineController {

  public void checkBudget(ActionRequest request, ActionResponse response) {
    try {
      Context parentContext = request.getContext().getParent();
      Move move = null;
      if (parentContext != null && Move.class.equals(parentContext.getContextClass())) {
        move = parentContext.asType(Move.class);
      } else {
        move = request.getContext().asType(MoveLine.class).getMove();
      }
      if (move != null && move.getCompany() != null) {
        response.setAttr(
            "budgetDistributionList",
            "readonly",
            !(Beans.get(BudgetToolsService.class)
                    .checkBudgetKeyAndRole(move.getCompany(), AuthUtils.getUser()))
                || move.getStatusSelect() == MoveRepository.STATUS_ACCOUNTED
                || move.getStatusSelect() == MoveRepository.STATUS_CANCELED);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.INFORMATION);
    }
  }

  public void validateBudgetLinesAmount(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);
      Beans.get(MoveLineBudgetService.class).checkAmountForMoveLine(moveLine);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.INFORMATION);
    }
  }
}
