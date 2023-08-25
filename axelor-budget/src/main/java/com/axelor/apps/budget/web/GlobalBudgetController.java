package com.axelor.apps.budget.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.apps.budget.service.BudgetLevelService;
import com.axelor.apps.budget.service.GlobalBudgetService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class GlobalBudgetController {
  public void checkBudgetDates(ActionRequest request, ActionResponse response) {
    try {
      GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
      if (globalBudget != null) {
        Beans.get(GlobalBudgetService.class).validateDates(globalBudget);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setDates(ActionRequest request, ActionResponse response) throws AxelorException {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);

    Beans.get(BudgetLevelService.class)
        .getUpdatedGroupBudgetLevelList(
            globalBudget.getBudgetLevelList(),
            globalBudget.getFromDate(),
            globalBudget.getToDate());
    response.setReload(true);
  }

  public void validateChildren(ActionRequest request, ActionResponse response) {
    try {
      GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
      globalBudget = Beans.get(GlobalBudgetRepository.class).find(globalBudget.getId());
      Beans.get(GlobalBudgetService.class).validateChildren(globalBudget);
      response.setValues(globalBudget);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void archiveChildren(ActionRequest request, ActionResponse response) {
    try {
      GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
      Beans.get(GlobalBudgetService.class).archiveChildren(globalBudget);
      response.setValues(globalBudget);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void draftChildren(ActionRequest request, ActionResponse response) {
    try {
      GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
      Beans.get(GlobalBudgetService.class).draftChildren(globalBudget);
      response.setValues(globalBudget);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void validateStructure(ActionRequest request, ActionResponse response) {
    try {
      GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
      globalBudget = Beans.get(GlobalBudgetRepository.class).find(globalBudget.getId());
      Beans.get(GlobalBudgetService.class).validateChildren(globalBudget);
      response.setValue("budgetLevelList", globalBudget.getBudgetLevelList());
      response.setValue(
          "statusSelect", GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_VALID_STRUCTURE);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void recomputeBudgetKey(ActionRequest request, ActionResponse response) {
    try {
      GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
      if (globalBudget.getId() != null) {
        Beans.get(GlobalBudgetService.class).generateBudgetKey(globalBudget);
        response.setValues(globalBudget);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
