package com.axelor.apps.gdpr.service;

import com.axelor.apps.gdpr.db.GDPRErasureResponse;
import com.axelor.apps.gdpr.db.GDPRRequest;
import com.axelor.exception.AxelorException;
import java.io.IOException;
import javax.mail.MessagingException;
import wslite.json.JSONException;

public interface GDPRErasureResponseService {

  public GDPRErasureResponse createErasureResponse(GDPRRequest gdprRequest)
      throws ClassNotFoundException, AxelorException;

  public void anonymizeTrackingDatas(GDPRRequest gdprRequest)
      throws ClassNotFoundException, JSONException, IOException;

  public boolean sendEmailResponse(GDPRErasureResponse GDPRErasureResponse)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          MessagingException, IOException, AxelorException, JSONException;
}
