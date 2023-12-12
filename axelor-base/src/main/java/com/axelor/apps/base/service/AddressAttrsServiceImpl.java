package com.axelor.apps.base.service;

import com.axelor.apps.base.db.AddressTemplateLine;
import com.axelor.meta.db.MetaField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressAttrsServiceImpl implements AddressAttrsService {

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void addHiddenAndTitle(
      List<AddressTemplateLine> addressTemplateLineList,
      Map<String, Map<String, Object>> attrsMap) {
    List<String> addressFormFieldsList =
            List.of("room", "floor", "streetNumber", "street", "streetName", "postBox", "city", "zip");

    if(addressTemplateLineList.isEmpty()){
      addressFormFieldsList.forEach(field -> addFieldUnhide(field, attrsMap));
    } else{
      addressFormFieldsList.forEach(field -> addFieldHide(field, attrsMap));
    }

    // Iterate through addressTemplateLineList and call addFieldTitle for each MetaField
    addressTemplateLineList.stream()
            .map(AddressTemplateLine::getMetaField)
            .forEach(metaField -> addFieldTitle(metaField.getName(), metaField.getLabel(), attrsMap));
  }

  @Override
  public void addFieldUnhide(String field, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(field, "hidden", false, attrsMap);
  }

  @Override
  public void addFieldHide(String field, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(field, "hidden", true, attrsMap);
  }

  @Override
  public void addFieldTitle(String field, String value, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(field, "title", value, attrsMap);
  }
}
