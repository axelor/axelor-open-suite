package com.axelor.apps.quality.web;

import com.axelor.apps.quality.db.ControlPoint;
import com.axelor.apps.quality.db.repo.ControlPointRepository;
import com.axelor.apps.quality.service.ControlPointWorkflowService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class ControlPointController {

  public void closeControlPoint(ActionRequest request, ActionResponse response) {
    try {
      ControlPoint controlPoint = request.getContext().asType(ControlPoint.class);
      controlPoint = Beans.get(ControlPointRepository.class).find(controlPoint.getId());
      Beans.get(ControlPointWorkflowService.class).closeControlPoint(controlPoint);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
