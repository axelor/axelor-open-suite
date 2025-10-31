package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import java.io.IOException;
import java.util.Map;

public interface KilometricResponseToolService {

  Map<String, Object> getApiResponse(String urlString, String exceptionMessage)
      throws IOException, AxelorException;
}
