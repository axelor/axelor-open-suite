package com.axelor.apps.base.service.address;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.AddressTemplateLine;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppBase;
import com.axelor.utils.service.TranslationBaseService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressAttrsServiceImpl implements AddressAttrsService {

  protected AddressMetaService addressMetaService;
  protected TranslationBaseService translationBaseService;

  @Inject
  public AddressAttrsServiceImpl(
      AddressMetaService addressMetaService, TranslationBaseService translationBaseService) {
    this.addressMetaService = addressMetaService;
    this.translationBaseService = translationBaseService;
  }

  @Override
  public Map<String, Map<String, Object>> getCountryAddressMetaFieldOnChangeAttrsMap(
      Address address) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();
    List<AddressTemplateLine> addressTemplateLineList =
        address.getCountry().getAddressTemplate().getAddressTemplateLineList();
    if (ObjectUtils.notEmpty(addressTemplateLineList)) {
      addHiddenAndTitle(addressTemplateLineList, attrsMap);
      addAllFieldsUnhide(addressTemplateLineList, attrsMap);
    }
    return attrsMap;
  }

  protected void addHiddenAndTitle(
      List<AddressTemplateLine> addressTemplateLineList,
      Map<String, Map<String, Object>> attrsMap) {

    addFieldHide("street", attrsMap);
    if (addressTemplateLineList.isEmpty()) {
      addressMetaService
          .getAddressFormFieldsList()
          .forEach(field -> addFieldUnhide(field, attrsMap));
    } else {
      addressMetaService.getAddressFormFieldsList().forEach(field -> addFieldHide(field, attrsMap));
    }

    // Iterate through addressTemplateLineList and call addFieldTitle for each MetaField
    addressTemplateLineList.stream()
        .filter(line -> !StringUtils.isBlank(line.getTitle()))
        .forEach(
            line ->
                addFieldTitle(
                    line.getMetaField().getName(),
                    translationBaseService.getValueTranslation(line.getTitle()),
                    attrsMap));
  }

  protected void addAllFieldsUnhide(
      List<AddressTemplateLine> addressTemplateLineList,
      Map<String, Map<String, Object>> attrsMap) {

    AppBase appBase = Beans.get(AppBaseService.class).getAppBase();

    addressTemplateLineList.forEach(
        line -> {
          addFieldUnhide(line.getMetaField().getName(), attrsMap);
          if ("streetName".equals(line.getMetaField().getName()) && appBase.getStoreStreets()) {
            addFieldUnhide("buildingNumber", attrsMap);
            addFieldUnhide("street", attrsMap);
            addFieldHide(line.getMetaField().getName(), attrsMap);
          }
          if (line.getIsRequired()) {
            addFieldRequired(line.getMetaField().getName(), attrsMap);
          }
        });
    addFieldUnhide("formattedFullName", attrsMap);
  }

  protected void addFieldRequired(String field, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(field, "required", true, attrsMap);
  }

  protected void addFieldUnhide(String field, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(field, "hidden", false, attrsMap);
  }

  protected void addFieldHide(String field, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(field, "hidden", true, attrsMap);
  }

  protected void addFieldTitle(
      String field, String value, Map<String, Map<String, Object>> attrsMap) {
    this.addAttr(field, "title", value, attrsMap);
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }
    attrsMap.get(field).put(attr, value);
  }
}
