package com.axelor.apps.gdpr.service.response;

import com.axelor.apps.gdpr.db.GDPRRequest;
import com.axelor.apps.gdpr.db.GDPRResponse;
import com.axelor.exception.AxelorException;
import java.io.IOException;

public interface GdprResponseAccessService {
  void generateAccessResponseDataFile(GDPRRequest gdprRequest)
      throws AxelorException, ClassNotFoundException, IOException;

  void sendEmailResponse(GDPRResponse gdprResponse) throws AxelorException;
}
