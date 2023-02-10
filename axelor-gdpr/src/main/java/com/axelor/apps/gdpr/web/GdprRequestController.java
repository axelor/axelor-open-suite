package com.axelor.apps.gdpr.web;

import com.axelor.apps.gdpr.db.GDPRRequest;
import com.axelor.apps.gdpr.db.repo.GDPRRequestRepository;
import com.axelor.apps.gdpr.service.response.GdprResponseService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class GdprRequestController {

  public void generateResponse(ActionRequest request, ActionResponse response) {
    try {
      GDPRRequest gdprRequest = request.getContext().asType(GDPRRequest.class);
      gdprRequest = Beans.get(GDPRRequestRepository.class).find(gdprRequest.getId());
      Beans.get(GdprResponseService.class).generateResponse(gdprRequest);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void sendResponse(ActionRequest request, ActionResponse response) {
    try {
      GDPRRequest gdprRequest = request.getContext().asType(GDPRRequest.class);
      gdprRequest = Beans.get(GDPRRequestRepository.class).find(gdprRequest.getId());
      Beans.get(GdprResponseService.class).sendResponse(gdprRequest);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
