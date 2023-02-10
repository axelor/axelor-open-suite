package com.axelor.apps.gdpr.service;

import com.axelor.exception.AxelorException;
import java.util.List;
import java.util.Map;

public interface GdprSearchEngineService {

  List<Map<String, Object>> searchObject(Map<String, Object> searchParams) throws AxelorException;

  Map<String, Object> checkSelectedObject(List<Map<String, Object>> resultList)
      throws AxelorException;
}
