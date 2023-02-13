package com.axelor.apps.gdpr.service.response;

import com.axelor.apps.gdpr.db.GDPRRequest;
import com.axelor.apps.gdpr.db.GDPRResponse;
import com.axelor.exception.AxelorException;
import java.io.IOException;

public interface GdprResponseErasureService {
  void createErasureResponse(GDPRRequest gdprRequest)
      throws ClassNotFoundException, AxelorException;

  void anonymizeTrackingDatas(GDPRRequest gdprRequest) throws ClassNotFoundException, IOException;

  void sendEmailResponse(GDPRResponse gdprResponse) throws AxelorException;
}
