package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.ClosureAssistantLine;
import com.axelor.apps.account.db.repo.ClosureAssistantLineRepository;
import com.axelor.apps.account.service.ClosureAssistantLineService;
import com.axelor.apps.supplychain.service.ClosureAssistantLineSupplychainServiceImpl;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.HashMap;
import java.util.Map;

public class ClosureAssistantSupplychainController {

  public void openViewLinkToAction(ActionRequest request, ActionResponse response) {

    try {
      ClosureAssistantLine closureAssistantLine =
          request.getContext().asType(ClosureAssistantLine.class);
      Map<String, Object> view = new HashMap<>();
      if (closureAssistantLine.getActionSelect()
          == ClosureAssistantLineRepository.ACTION_CUT_OF_GENERATION) {
        view =
            Beans.get(ClosureAssistantLineSupplychainServiceImpl.class)
                .getViewToOpen(closureAssistantLine);
      } else {
        view = Beans.get(ClosureAssistantLineService.class).getViewToOpen(closureAssistantLine);
      }
      if (!view.equals(null)) {
        response.setView(view);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
