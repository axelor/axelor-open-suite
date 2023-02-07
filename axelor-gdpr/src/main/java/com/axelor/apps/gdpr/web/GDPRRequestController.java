package com.axelor.apps.gdpr.web;

import com.axelor.apps.gdpr.db.GDPRAccessResponse;
import com.axelor.apps.gdpr.db.GDPRErasureResponse;
import com.axelor.apps.gdpr.db.GDPRRequest;
import com.axelor.apps.gdpr.db.repo.GDPRRequestRepository;
import com.axelor.apps.gdpr.service.GDPRAccessResponseService;
import com.axelor.apps.gdpr.service.GDPRErasureResponseService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import javax.mail.MessagingException;
import wslite.json.JSONException;

public class GDPRRequestController {

  protected GDPRRequestRepository gdprRequestRepository;
  protected GDPRAccessResponseService gdprAccessResponseService;
  protected GDPRErasureResponseService gdprErasureResponseService;

  @Inject
  public GDPRRequestController(
      GDPRRequestRepository gdprRequestRepository,
      GDPRAccessResponseService gdprAccessResponseService,
      GDPRErasureResponseService gdprErasureResponseService) {
    this.gdprRequestRepository = gdprRequestRepository;
    this.gdprAccessResponseService = gdprAccessResponseService;
    this.gdprErasureResponseService = gdprErasureResponseService;
  }

  public void generateResponse(ActionRequest request, ActionResponse response) {
    GDPRRequest gdprRequest = request.getContext().asType(GDPRRequest.class);
    gdprRequest = gdprRequestRepository.find(gdprRequest.getId());

    if (gdprRequest.getTypeSelect() == GDPRRequestRepository.REQUEST_TYPE_ACCESS) {
      try {
        GDPRAccessResponse gdprAccessResponse =
            gdprAccessResponseService.generateAccessResponseDataFile(gdprRequest);
        response.setValue("AccessResponse", gdprAccessResponse);
      } catch (ClassNotFoundException | AxelorException | IOException e) {
        TraceBackService.trace(e);
      }
    } else {
      try {
        GDPRErasureResponse gdprErasureResponse =
            gdprErasureResponseService.createErasureResponse(gdprRequest);
        gdprErasureResponseService.anonymizeTrackingDatas(gdprRequest);
        response.setValue("ErasureResponse", gdprErasureResponse);
      } catch (AxelorException | ClassNotFoundException | JSONException | IOException e) {
        TraceBackService.trace(e);
      }
    }

    gdprRequest.setStatusSelect(GDPRRequestRepository.REQUEST_STATUS_CONFIRMED);
    saveGdprRequest(gdprRequest);
    response.setReload(true);
  }

  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void saveGdprRequest(GDPRRequest gdprRequest) {
    gdprRequestRepository.save(gdprRequest);
  }

  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void sendResponse(ActionRequest request, ActionResponse response) throws AxelorException {
    GDPRRequest gdprRequest = request.getContext().asType(GDPRRequest.class);
    gdprRequest = gdprRequestRepository.find(gdprRequest.getId());

    boolean success;

    try {
      if (gdprRequest.getTypeSelect() == GDPRRequestRepository.REQUEST_TYPE_ACCESS) {
        success = gdprAccessResponseService.sendEmailResponse(gdprRequest.getAccessResponse());
      } else {
        success = gdprErasureResponseService.sendEmailResponse(gdprRequest.getErasureResponse());
      }
    } catch (ClassNotFoundException
        | InstantiationException
        | IllegalAccessException
        | AxelorException
        | MessagingException
        | IOException
        | JSONException e) {
      TraceBackService.trace(e);
      throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }

    if (success) {
      gdprRequest.setStatusSelect(GDPRRequestRepository.REQUEST_STATUS_SENT);
      gdprRequestRepository.save(gdprRequest);
      response.setReload(true);
    } else {
      response.setError(I18n.get("Missing mail template"));
    }
  }
}
