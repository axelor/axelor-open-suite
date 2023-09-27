package com.axelor.apps.budget.web;

import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.GlobalBudgetTemplate;
import com.axelor.apps.budget.service.GlobalBudgetService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class GlobalBudgetTemplateController {

  public void generateGlobalBudget(ActionRequest request, ActionResponse response) {
    try {
      GlobalBudgetTemplate globalBudgetTemplate =
          request.getContext().asType(GlobalBudgetTemplate.class);
      if (globalBudgetTemplate != null) {
        GlobalBudget globalBudget =
            Beans.get(GlobalBudgetService.class)
                .generateGlobalBudgetWithTemplate(globalBudgetTemplate);
        if (globalBudget != null) {
          response.setView(
              ActionView.define(I18n.get("Global budget"))
                  .model(GlobalBudget.class.getName())
                  .add("grid", "global-budget-grid")
                  .add("form", "global-budget-form")
                  .context("_showRecord", String.valueOf(globalBudget.getId()))
                  .map());
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
