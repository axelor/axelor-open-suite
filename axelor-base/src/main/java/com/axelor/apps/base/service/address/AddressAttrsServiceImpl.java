/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.address;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.AddressTemplateLine;
import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.CityRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.Query;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppBase;
import com.axelor.utils.service.TranslationBaseService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class AddressAttrsServiceImpl implements AddressAttrsService {

  protected AddressMetaService addressMetaService;
  protected TranslationBaseService translationBaseService;

  protected CityRepository cityRepository;

  @Inject
  public AddressAttrsServiceImpl(
      AddressMetaService addressMetaService,
      TranslationBaseService translationBaseService,
      CityRepository cityRepository) {
    this.addressMetaService = addressMetaService;
    this.translationBaseService = translationBaseService;
    this.cityRepository = cityRepository;
  }

  protected boolean ifCountryHasCities(Country country) {
    Query<City> cityQuery =
        cityRepository.all().filter("self.country = :country").bind("country", country);
    List<City> cityList = cityQuery.fetch();
    return !CollectionUtils.isEmpty(cityList);
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

  @Override
  public Map<String, Map<String, Object>> getTownNameAndCityAttrsMap(
      Map<String, Map<String, Object>> countryAddressMetaFieldOnChangeAttrsMap, Address address) {
    Map<String, Object> cityAttrs = countryAddressMetaFieldOnChangeAttrsMap.get("city");
    Boolean cityHidden = (Boolean) cityAttrs.get("hidden");

    boolean ifCountryHasCities = ifCountryHasCities(address.getCountry());

    Map<String, Map<String, Object>> outerMap = new HashMap<>();
    Map<String, Object> townNameAndCityPanelMap = new HashMap<>();
    Map<String, Object> cityPanelMap = new HashMap<>();
    Map<String, Object> cityMap = new HashMap<>();

    if (countryAddressMetaFieldOnChangeAttrsMap.get("townName") != null) {
      Map<String, Object> townNameAttrs = countryAddressMetaFieldOnChangeAttrsMap.get("townName");
      if (townNameAttrs.containsKey("hidden")) {
        Boolean townNameHidden = (Boolean) townNameAttrs.get("hidden");
        if (!townNameHidden && ifCountryHasCities) {
          // show townName and city in townNameAndCityPanel
          townNameAndCityPanelMap.put("hidden", false);
          cityPanelMap.put("hidden", true);
          cityMap.put("hidden", false);
        } else if (!townNameHidden) {
          // show townName but don't show city in townNameAndCityPanel
          townNameAndCityPanelMap.put("hidden", false);
          cityPanelMap.put("hidden", true);
          cityMap.put("hidden", true);
        } else {
          // only show cityPanel and keep the previous setting.
          townNameAndCityPanelMap.put("hidden", true);
          cityPanelMap.put("hidden", false);
          cityMap.put("hidden", cityHidden);
        }
        outerMap.put("townNameAndCityPanel", townNameAndCityPanelMap);
        outerMap.put("cityPanel", cityPanelMap);
        outerMap.put("city", cityMap);
      }
    }
    return outerMap;
  }

  @Override
  public void updateAttrsMap(
      Map<String, Map<String, Object>> townNameAndCityAttrtsMap,
      Map<String, Map<String, Object>> countryAddressMetaFieldOnChangeAttrsMap) {
    for (Map.Entry<String, Map<String, Object>> entry2 : townNameAndCityAttrtsMap.entrySet()) {
      String key2 = entry2.getKey();
      Map<String, Object> innerMap2 = entry2.getValue();

      // Check if map1 contains the same key as map2
      if (countryAddressMetaFieldOnChangeAttrsMap.containsKey(key2)) {
        Map<String, Object> innerMap1 = countryAddressMetaFieldOnChangeAttrsMap.get(key2);

        // Check each key-value pair in the inner map of map2
        for (Map.Entry<String, Object> innerEntry2 : innerMap2.entrySet()) {
          String innerKey2 = innerEntry2.getKey();
          Object innerValue2 = innerEntry2.getValue();

          // If map1's inner map contains the same key as map2's inner map, update the value
          if (innerMap1.containsKey(innerKey2)) {
            innerMap1.put(innerKey2, innerValue2); // Update the value in map1
          } else {
            innerMap1.put(innerKey2, innerValue2); // Add new key-value pair to inner map
          }
        }
      } else {
        // If map1 doesn't contain the key, add the entire key-value pair from map2
        countryAddressMetaFieldOnChangeAttrsMap.put(key2, new HashMap<>(innerMap2));
      }
    }
  }
}
