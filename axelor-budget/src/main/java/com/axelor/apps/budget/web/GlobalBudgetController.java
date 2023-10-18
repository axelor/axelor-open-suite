package com.axelor.apps.budget.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.db.BudgetVersion;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.BudgetVersionRepository;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.apps.budget.service.BudgetLevelService;
import com.axelor.apps.budget.service.BudgetVersionService;
import com.axelor.apps.budget.service.GlobalBudgetGroupService;
import com.axelor.apps.budget.service.GlobalBudgetService;
import com.axelor.apps.budget.service.GlobalBudgetWorkflowService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Map;

public class GlobalBudgetController {

  @ErrorException
  public void checkBudgetDates(ActionRequest request, ActionResponse response)
      throws AxelorException {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    if (globalBudget != null) {
      Beans.get(GlobalBudgetService.class).validateDates(globalBudget);
    }
  }

  @ErrorException
  public void setDates(ActionRequest request, ActionResponse response) throws AxelorException {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);

    Beans.get(BudgetLevelService.class)
        .getUpdatedGroupBudgetLevelList(
            globalBudget.getBudgetLevelList(),
            globalBudget.getFromDate(),
            globalBudget.getToDate());
    response.setReload(true);
  }

  @ErrorException
  public void validateChildren(ActionRequest request, ActionResponse response)
      throws AxelorException {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    Beans.get(GlobalBudgetWorkflowService.class)
        .validateChildren(globalBudget, GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_VALID);
    response.setValues(globalBudget);
  }

  public void archiveChildren(ActionRequest request, ActionResponse response) {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);
    Beans.get(GlobalBudgetWorkflowService.class).archiveChildren(globalBudget);
    response.setValues(globalBudget);
  }

  public void draftChildren(ActionRequest request, ActionResponse response) {
    GlobalBudget globalBudget =
        Beans.get(GlobalBudgetRepository.class)
            .find(request.getContext().asType(GlobalBudget.class).getId());
    Beans.get(GlobalBudgetWorkflowService.class).draftChildren(globalBudget);
    response.setReload(true);
  }

  @ErrorException
  public void validateStructure(ActionRequest request, ActionResponse response)
      throws AxelorException {
    GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);

    Beans.get(GlobalBudgetGroupService.class).validateStructure(globalBudget);
    response.setValues(globalBudget);
  }

  public void createNewBudgetVersion(ActionRequest request, ActionResponse response) {
    Map<String, Object> partialGlobalBudget =
        (Map<String, Object>) request.getContext().get("_globalBudget");
    GlobalBudget globalBudget =
        Beans.get(GlobalBudgetRepository.class)
            .find(Long.valueOf(partialGlobalBudget.get("id").toString()));
    if (globalBudget != null) {
      String versionName = (String) request.getContext().get("name");
      BudgetVersion budgetVersion =
          Beans.get(BudgetVersionService.class).createNewVersion(globalBudget, versionName);

      response.setView(
          ActionView.define(I18n.get("Budget version"))
              .model(BudgetVersion.class.getName())
              .add("grid", "budget-version-grid")
              .add("form", "budget-version-form")
              .context("_showRecord", String.valueOf(budgetVersion.getId()))
              .map());
      response.setCanClose(true);
    }
  }

  public void createDefaultBudgetVersion(ActionRequest request, ActionResponse response) {
    try {
      GlobalBudget globalBudget = request.getContext().asType(GlobalBudget.class);

      if (globalBudget != null
          && globalBudget.getId() != null
          && globalBudget.getActiveVersion() == null) {
        globalBudget = Beans.get(GlobalBudgetRepository.class).find(globalBudget.getId());
        BudgetVersion budgetVersion =
            Beans.get(BudgetVersionService.class)
                .createNewVersion(globalBudget, globalBudget.getName());
        globalBudget =
            Beans.get(GlobalBudgetService.class).changeBudgetVersion(globalBudget, budgetVersion);
        response.setReload(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @ErrorException
  public void changeBudgetVersion(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Map<String, Object> partialGlobalBudget =
        (Map<String, Object>) request.getContext().get("_globalBudget");
    Map<String, Object> partialBudgetVersion =
        (Map<String, Object>) request.getContext().get("budgetVersion");
    GlobalBudget globalBudget =
        Beans.get(GlobalBudgetRepository.class)
            .find(Long.valueOf(partialGlobalBudget.get("id").toString()));
    BudgetVersion selectedVersion =
        Beans.get(BudgetVersionRepository.class)
            .find(Long.valueOf(partialBudgetVersion.get("id").toString()));
    globalBudget =
        Beans.get(GlobalBudgetService.class).changeBudgetVersion(globalBudget, selectedVersion);
  }

  public void setBudgetVersionDomain(ActionRequest request, ActionResponse response) {
    Map<String, Object> partialGlobalBudget =
        (Map<String, Object>) request.getContext().get("_globalBudget");
    GlobalBudget globalBudget =
        Beans.get(GlobalBudgetRepository.class)
            .find(Long.valueOf(partialGlobalBudget.get("id").toString()));
    String domain = "self.id = 0";
    if (globalBudget != null) {
      domain =
          String.format(
              "self.isActive = false  AND self.globalBudget.id = %s", globalBudget.getId());
    }
    response.setAttr("$budgetVersion", "domain", domain);
  }
}
