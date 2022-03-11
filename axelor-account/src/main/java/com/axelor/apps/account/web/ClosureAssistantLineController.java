package com.axelor.apps.account.web;

import com.axelor.apps.account.db.ClosureAssistantLine;
import com.axelor.apps.account.db.repo.ClosureAssistantLineRepository;
import com.axelor.apps.account.service.ClosureAssistantLineService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ClosureAssistantLineController {

  public void validateClosureAssistantLine(ActionRequest request, ActionResponse response) {

    try {
      ClosureAssistantLine closureAssistantLine =
          request.getContext().asType(ClosureAssistantLine.class);
      closureAssistantLine =
          Beans.get(ClosureAssistantLineRepository.class).find(closureAssistantLine.getId());
      Beans.get(ClosureAssistantLineService.class)
          .validateClosureAssistantLine(closureAssistantLine);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancelClosureAssistantLine(ActionRequest request, ActionResponse response) {

    try {
      ClosureAssistantLine closureAssistantLine =
          request.getContext().asType(ClosureAssistantLine.class);
      closureAssistantLine =
          Beans.get(ClosureAssistantLineRepository.class).find(closureAssistantLine.getId());
      Beans.get(ClosureAssistantLineService.class).cancelClosureAssistantLine(closureAssistantLine);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void openViewLinkToAction(ActionRequest request, ActionResponse response) {

    try {
      ClosureAssistantLine closureAssistantLine =
          request.getContext().asType(ClosureAssistantLine.class);
      if (closureAssistantLine.getActionSelect()
          == ClosureAssistantLineRepository.ACTION_FIXED_ASSET_REALIZATION) {

      } else if (closureAssistantLine.getActionSelect()
          == ClosureAssistantLineRepository.ACTION_MOVE_CONSISTENCY_CHECK) {

      } else if (closureAssistantLine.getActionSelect()
          == ClosureAssistantLineRepository.ACTION_ACCOUNTING_REPORTS) {

      } else if (closureAssistantLine.getActionSelect()
          == ClosureAssistantLineRepository.ACTION_CALCULATE_THE_OUTRUN_OF_THE_YEAR) {

      } else if (closureAssistantLine.getActionSelect()
          == ClosureAssistantLineRepository.ACTION_CLOSURE_AND_OPENING_OF_FISCAL_YEAR_BATCH) {

      } else if (closureAssistantLine.getActionSelect()
          == ClosureAssistantLineRepository.ACTION_FISCAL_YEAR_CLOSURE) {

      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
