package com.axelor.apps.base.service;

import java.util.Map;

public interface AddressTemplateLineService {
  public Map<String, Map<String, Object>> getAddressTemplateFieldsForMetaField(String field);
}
