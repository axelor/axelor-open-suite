package com.axelor.apps.gdpr.service;

import com.axelor.exception.AxelorException;
import java.util.List;
import java.util.Map;

public interface GdprSearchEngineService {

  public List<Map<String, Object>> searchObject(Map<String, Object> searchParams)
      throws AxelorException;
}
