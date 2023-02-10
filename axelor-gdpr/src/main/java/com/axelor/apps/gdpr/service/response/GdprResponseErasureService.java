package com.axelor.apps.gdpr.service.response;

import com.axelor.apps.gdpr.db.GDPRRequest;
import com.axelor.apps.gdpr.db.GDPRResponse;
import com.axelor.exception.AxelorException;
import java.io.IOException;
import javax.mail.MessagingException;
import wslite.json.JSONException;

public interface GdprResponseErasureService {
  void createErasureResponse(GDPRRequest gdprRequest)
      throws ClassNotFoundException, AxelorException;

  void anonymizeTrackingDatas(GDPRRequest gdprRequest)
      throws ClassNotFoundException, JSONException, IOException;

  boolean sendEmailResponse(GDPRResponse gdprResponse)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          MessagingException, IOException, AxelorException, JSONException;
}
