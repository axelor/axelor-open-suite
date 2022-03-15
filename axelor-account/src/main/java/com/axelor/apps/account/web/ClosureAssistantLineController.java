package com.axelor.apps.account.web;

import com.axelor.apps.account.db.ClosureAssistant;
import com.axelor.apps.account.db.ClosureAssistantLine;
import com.axelor.apps.account.db.repo.ClosureAssistantLineRepository;
import com.axelor.apps.account.db.repo.ClosureAssistantRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.ClosureAssistantLineService;
import com.axelor.apps.account.service.ClosureAssistantService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Map;

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
          == ClosureAssistantLineRepository.ACTION_FISCAL_YEAR_CLOSURE) {
        response.setAlert(IExceptionMessage.ACCOUNT_CLOSURE_ASSISTANT_FISCAL_YEAR_CLOSURE);
      }

      Map<String, Object> view =
          Beans.get(ClosureAssistantLineService.class).getViewToOpen(closureAssistantLine);

      if (view != null) {
        response.setView(view);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkNoExistingClosureAssistantForSameYear(
      ActionRequest request, ActionResponse response) {

    try {

      ClosureAssistantLine closureAssistantLine =
          request.getContext().asType(ClosureAssistantLine.class);
      ClosureAssistant closureAssistant = closureAssistantLine.getClosureAssistant();
      if (closureAssistant.getId() != null) {
        closureAssistant =
            Beans.get(ClosureAssistantRepository.class).find(closureAssistant.getId());
      }

      if (Beans.get(ClosureAssistantService.class)
          .checkNoExistingClosureAssistantForSameYear(closureAssistant)) {
        response.setException(
            new AxelorException(
                TraceBackRepository.CATEGORY_INCONSISTENCY,
                I18n.get(IExceptionMessage.ACCOUNT_CLOSURE_ASSISTANT_ALREADY_EXISTS_FOR_SAME_YEAR),
                closureAssistant.getFiscalYear().getCode(),
                closureAssistant.getCompany().getCode()));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
