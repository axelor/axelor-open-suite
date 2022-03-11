package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.ClosureAssistantLine;
import com.axelor.apps.account.db.repo.ClosureAssistantLineRepository;
import com.axelor.apps.base.db.Batch;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ClosureAssistantSupplychainController {

  public void openViewLinkToAction(ActionRequest request, ActionResponse response) {

    try {
      ClosureAssistantLine closureAssistantLine =
          request.getContext().asType(ClosureAssistantLine.class);
      if (closureAssistantLine.getActionSelect()
          == ClosureAssistantLineRepository.ACTION_CUT_OF_GENERATION) {
        response.setView(
            ActionView.define("Invoice")
                .model(Batch.class.getName())
                .add("grid", "invoice-grid")
                .add("form", "invoice-form")
                .param("search-filters", "customer-invoices-filters")
                .param("forceEdit", Boolean.TRUE.toString())
                .map());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
