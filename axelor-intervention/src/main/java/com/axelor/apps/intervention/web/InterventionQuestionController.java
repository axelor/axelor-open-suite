package com.axelor.apps.intervention.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.intervention.db.InterventionQuestion;
import com.axelor.apps.intervention.service.InterventionQuestionService;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class InterventionQuestionController {

  public void advancedMonitoringAnswer(ActionRequest request, ActionResponse response) {
    try {
      InterventionQuestion interventionQuestion =
          request.getContext().asType(InterventionQuestion.class);
      Beans.get(InterventionQuestionService.class).advancedMonitoringAnswer(interventionQuestion);
      response.setValues(Mapper.toMap(interventionQuestion));
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }
}
