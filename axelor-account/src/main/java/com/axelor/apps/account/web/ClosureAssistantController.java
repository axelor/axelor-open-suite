package com.axelor.apps.account.web;

import com.axelor.apps.account.db.ClosureAssistant;
import com.axelor.apps.account.db.ClosureAssistantLine;
import com.axelor.apps.account.service.ClosureAssistantLineService;
import com.axelor.apps.base.db.Year;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;

public class ClosureAssistantController {

  public void setFiscalYear(ActionRequest request, ActionResponse response) {

    try {
      ClosureAssistant closureAssistant = request.getContext().asType(ClosureAssistant.class);
      AuthUtils.getUser();

      Year fiscalYear = null;

      response.setValue("fiscalYear", fiscalYear);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void initClosureAssistantLines(ActionRequest request, ActionResponse response) {

    try {
      ClosureAssistant closureAssistant = request.getContext().asType(ClosureAssistant.class);
      List<ClosureAssistantLine> closureAssistantLineList =
          Beans.get(ClosureAssistantLineService.class).initClosureAssistantLines(closureAssistant);

      response.setValue("closureAssistantLineList", closureAssistantLineList);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkNoExistingClosureAssistantLineForSameYear(
      ActionRequest request, ActionResponse response) {

    try {
      ClosureAssistant closureAssistant = request.getContext().asType(ClosureAssistant.class);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
