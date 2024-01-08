package com.axelor.apps.base.service;

import com.axelor.apps.base.db.AddressTemplateLine;
import com.axelor.common.StringUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AddressAttrsServiceImpl implements AddressAttrsService {
  private static final List<String> addressFormFieldsList =
      List.of(
          "department",
          "subDepartment",
          "buildingName",
          "townName",
          "townLocationName",
          "districtName",
          "countrySubDivision",
          "room",
          "floor",
          "buildingNumber",
          "street",
          "streetName",
          "postBox",
          "city",
          "zip");

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void addAddressDomainFields(String field, Map<String, Map<String, Object>> attrsMap) {

    String nameList =
        addressFormFieldsList.stream()
            .map(item -> "'" + item + "'")
            .collect(Collectors.joining(","));
    addAttr(
        field,
        "domain",
        "self.metaModel.name = 'Address' AND  self.name IN (" + nameList + ")",
        attrsMap);
  }

  @Override
  public void addHiddenAndTitle(
      List<AddressTemplateLine> addressTemplateLineList,
      Map<String, Map<String, Object>> attrsMap) {

    if (addressTemplateLineList.isEmpty()) {
      addressFormFieldsList.forEach(field -> addFieldUnhide(field, attrsMap));
    } else {
      addressFormFieldsList.forEach(field -> addFieldHide(field, attrsMap));
    }

    // Iterate through addressTemplateLineList and call addFieldTitle for each MetaField
    addressTemplateLineList.stream()
        .filter(line -> !StringUtils.isBlank(line.getTitle()))
        .forEach(line -> addFieldTitle(line.getMetaField().getName(), line.getTitle(), attrsMap));
  }

  @Override
  public void addAllFieldsUnhide(
      List<AddressTemplateLine> addressTemplateLineList,
      Map<String, Map<String, Object>> attrsMap) {
    addressTemplateLineList.forEach(
        line -> {
          addFieldUnhide(line.getMetaField().getName(), attrsMap);
          if ("street".equals(line.getMetaField().getName())) {
            addFieldUnhide("buildingNumber", attrsMap);
          }
          if (line.getIsRequired()) {
            addFieldRequired(line.getMetaField().getName(), attrsMap);
          }
        });
    addFieldUnhide("formattedFullName", attrsMap);
  }

  @Override
  public void addFieldRequired(String field, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(field, "required", true, attrsMap);
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
