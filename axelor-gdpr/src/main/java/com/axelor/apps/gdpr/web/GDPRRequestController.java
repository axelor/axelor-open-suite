package com.axelor.apps.gdpr.web;

import com.axelor.apps.gdpr.db.GDPRAccessResponse;
import com.axelor.apps.gdpr.db.GDPRErasureResponse;
import com.axelor.apps.gdpr.db.GDPRRequest;
import com.axelor.apps.gdpr.db.repo.GDPRRequestRepository;
import com.axelor.apps.gdpr.service.GDPRAccessResponseService;
import com.axelor.apps.gdpr.service.GDPRErasureResponseService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;

public class GDPRRequestController {

  public void generateResponse(ActionRequest request, ActionResponse response) {
    try {
      GDPRRequest gdprRequest = request.getContext().asType(GDPRRequest.class);
      gdprRequest = Beans.get(GDPRRequestRepository.class).find(gdprRequest.getId());
      GDPRErasureResponseService gdprErasureResponseService =
          Beans.get(GDPRErasureResponseService.class);

      if (gdprRequest.getTypeSelect() == GDPRRequestRepository.REQUEST_TYPE_ACCESS) {
        GDPRAccessResponse gdprAccessResponse =
            Beans.get(GDPRAccessResponseService.class).generateAccessResponseDataFile(gdprRequest);
        response.setValue("AccessResponse", gdprAccessResponse);
      } else {
        GDPRErasureResponse gdprErasureResponse =
            gdprErasureResponseService.createErasureResponse(gdprRequest);
        gdprErasureResponseService.anonymizeTrackingDatas(gdprRequest);
        response.setValue("ErasureResponse", gdprErasureResponse);
      }

      gdprRequest.setStatusSelect(GDPRRequestRepository.REQUEST_STATUS_CONFIRMED);
      saveGdprRequest(gdprRequest);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void saveGdprRequest(GDPRRequest gdprRequest) {
    Beans.get(GDPRRequestRepository.class).save(gdprRequest);
  }

  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void sendResponse(ActionRequest request, ActionResponse response) {
    try {
      GDPRRequest gdprRequest = request.getContext().asType(GDPRRequest.class);
      GDPRRequestRepository gdprRequestRepository = Beans.get(GDPRRequestRepository.class);
      gdprRequest = gdprRequestRepository.find(gdprRequest.getId());
      boolean success;

      if (gdprRequest.getTypeSelect() == GDPRRequestRepository.REQUEST_TYPE_ACCESS) {
        success =
            Beans.get(GDPRAccessResponseService.class)
                .sendEmailResponse(gdprRequest.getAccessResponse());
      } else {
        success =
            Beans.get(GDPRErasureResponseService.class)
                .sendEmailResponse(gdprRequest.getErasureResponse());
      }
      if (success) {
        gdprRequest.setStatusSelect(GDPRRequestRepository.REQUEST_STATUS_SENT);
        gdprRequestRepository.save(gdprRequest);
        response.setReload(true);
      } else {
        response.setError(I18n.get("Missing mail template"));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
