package com.axelor.apps.base.service;

import com.axelor.apps.base.db.AddressTemplateLine;
import java.util.List;
import java.util.Map;

public interface AddressAttrsService {

  public void addHiddenAndTitle(
      List<AddressTemplateLine> address, Map<String, Map<String, Object>> attrsMap);

  public void addFieldUnhide(String field, Map<String, Map<String, Object>> attrsMap);

  public void addFieldHide(String field, Map<String, Map<String, Object>> attrsMap);

  public void addFieldTitle(String field, String value, Map<String, Map<String, Object>> attrsMap);
}
