package com.axelor.apps.crm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Map;

public interface LeadActivityService {
  List<Map<String, Object>> getLeadActivityData(Long id) throws JsonProcessingException;
}
