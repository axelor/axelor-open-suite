package com.axelor.apps.account.web;

import com.axelor.apps.account.db.ClosureAssistant;
import com.axelor.apps.account.db.ClosureAssistantLine;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.ClosureAssistantLineService;
import com.axelor.apps.account.service.ClosureAssistantService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;

public class ClosureAssistantController {

  public void setClosureAssistantFields(ActionRequest request, ActionResponse response) {

    try {
      ClosureAssistant closureAssistant = request.getContext().asType(ClosureAssistant.class);
      ClosureAssistantService closureAssistantService = Beans.get(ClosureAssistantService.class);

      closureAssistant = closureAssistantService.updateCompany(closureAssistant);

      closureAssistant = closureAssistantService.updateFiscalYear(closureAssistant);

      List<ClosureAssistantLine> closureAssistantLineList =
          Beans.get(ClosureAssistantLineService.class).initClosureAssistantLines(closureAssistant);

      response.setValue("company", closureAssistant.getCompany());
      response.setValue("fiscalYear", closureAssistant.getFiscalYear());
      response.setValue("closureAssistantLineList", closureAssistantLineList);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkNoExistingClosureAssistantForSameYear(
      ActionRequest request, ActionResponse response) {

    try {
      ClosureAssistant closureAssistant = request.getContext().asType(ClosureAssistant.class);
      if (Beans.get(ClosureAssistantService.class)
          .checkNoExistingClosureAssistantForSameYear(closureAssistant)) {
        response.setError(
            I18n.get(
                String.format(
                    AccountExceptionMessage.ACCOUNT_CLOSURE_ASSISTANT_ALREADY_EXISTS_FOR_SAME_YEAR,
                    closureAssistant.getFiscalYear().getCode(),
                    closureAssistant.getCompany().getCode())));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
