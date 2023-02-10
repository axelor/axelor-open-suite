package com.axelor.apps.gdpr.service.response;

import com.axelor.apps.gdpr.db.GDPRRequest;
import com.axelor.apps.gdpr.db.GDPRResponse;
import com.axelor.exception.AxelorException;
import java.io.IOException;
import wslite.json.JSONException;

public interface GdprResponseAccessService {
  void generateAccessResponseDataFile(GDPRRequest gdprRequest)
      throws AxelorException, ClassNotFoundException, IOException;

  boolean sendEmailResponse(GDPRResponse gdprResponse)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, IOException, JSONException;
}
