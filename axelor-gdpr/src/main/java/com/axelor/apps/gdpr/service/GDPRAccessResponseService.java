package com.axelor.apps.gdpr.service;

import com.axelor.apps.gdpr.db.GDPRAccessResponse;
import com.axelor.apps.gdpr.db.GDPRRequest;
import com.axelor.exception.AxelorException;
import java.io.IOException;
import wslite.json.JSONException;

public interface GDPRAccessResponseService {
  public GDPRAccessResponse generateAccessResponseDataFile(GDPRRequest gdprRequest)
      throws ClassNotFoundException, AxelorException, IOException;

  public boolean sendEmailResponse(GDPRAccessResponse gdprAccessResponse)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, IOException, JSONException;
}
