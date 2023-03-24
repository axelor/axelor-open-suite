package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ResearchRequest;
import com.axelor.apps.base.db.ResearchResultLine;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.research.ResearchRequestService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResearchRequestController {

  public void searchObject(ActionRequest request, ActionResponse response) {
    ResearchRequest researchRequest = request.getContext().asType(ResearchRequest.class);

    // keep only filled fields
    Map<String, Object> searchParams = new HashMap<>();
    if (researchRequest.getResearch1() != null) {
      searchParams.put(
          researchRequest.getResearch1().getCode(), researchRequest.getResearch1Value());
    }
    if (researchRequest.getResearch2() != null) {
      searchParams.put(
          researchRequest.getResearch2().getCode(), researchRequest.getResearch2Value());
    }
    if (researchRequest.getResearch3() != null) {
      searchParams.put(
          researchRequest.getResearch3().getCode(), researchRequest.getResearch3Value());
    }
    if (researchRequest.getResearch4() != null) {
      searchParams.put(
          researchRequest.getResearch4().getCode(), researchRequest.getResearch4Value());
    }
    if (researchRequest.getDateResearch1() != null) {
      searchParams.put(
          researchRequest.getDateResearch1().getCode(), researchRequest.getDateResearch1Value());
    }

    if (searchParams.isEmpty()) {
      response.setAlert(I18n.get("Please enter at least one field."));
    } else {
      List<ResearchResultLine> resultList = new ArrayList<>();
      try {
        resultList =
            Beans.get(ResearchRequestService.class).searchObject(searchParams, researchRequest);
      } catch (AxelorException e) {
        TraceBackService.trace(e);
        response.setError(e.getMessage());
      }
      response.setValue("researchResultLineList", resultList);
      response.setValue("searchDate", Beans.get(AppBaseService.class).getTodayDate(null));
    }
  }
}
