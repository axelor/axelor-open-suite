package com.axelor.apps.crm.service;

import com.axelor.apps.base.AxelorException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Map;

public interface CrmActivityService {
  List<Map<String, Object>> getLeadActivityData(Long id) throws JsonProcessingException;

  List<Map<String, Object>> getPartnerActivityData(Long id)
      throws JsonProcessingException, AxelorException;
}
