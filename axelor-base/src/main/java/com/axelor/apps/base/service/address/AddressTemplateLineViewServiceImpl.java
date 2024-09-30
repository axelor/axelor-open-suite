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

import com.axelor.apps.base.db.AddressTemplateLine;
import com.axelor.apps.base.db.Country;
import com.axelor.meta.CallMethod;
import com.google.api.client.util.Strings;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class AddressTemplateLineViewServiceImpl implements AddressTemplateLineViewService {

  protected AddressMetaService addressMetaService;

  @Inject
  public AddressTemplateLineViewServiceImpl(AddressMetaService addressMetaService) {
    this.addressMetaService = addressMetaService;
  }

  @Override
  public String computeMetaFieldDomain() {
    String nameList =
        addressMetaService.getAddressFormFieldsList().stream()
            .map(item -> "'" + item + "'")
            .collect(Collectors.joining(","));
    return "self.metaModel.name = 'Address' AND  self.name IN (" + nameList + ")";
  }

  @CallMethod
  public static Boolean fieldNotInAddressTemplate(String fieldName, Country country) {
    if (Strings.isNullOrEmpty(fieldName) || country == null) return true;
    List<AddressTemplateLine> addressTemplateLineList =
        country.getAddressTemplate().getAddressTemplateLineList();

    return getAddressTemplateLine(fieldName, addressTemplateLineList) == null;
  }

  @CallMethod
  public static Boolean fieldIsRequiredOnAddressTemplate(String fieldName, Country country) {
    if (Strings.isNullOrEmpty(fieldName) || country == null) return false;
    List<AddressTemplateLine> addressTemplateLineList =
        country.getAddressTemplate().getAddressTemplateLineList();
    AddressTemplateLine addressTemplateLine =
        getAddressTemplateLine(fieldName, addressTemplateLineList);
    if (addressTemplateLine == null) return false;
    return addressTemplateLine.getIsRequired();
  }

  @CallMethod
  public static AddressTemplateLine getAddressTemplateLine(
      String fieldName, List<AddressTemplateLine> addressTemplateLineList) {
    if (Strings.isNullOrEmpty(fieldName)
        || addressTemplateLineList == null
        || addressTemplateLineList.isEmpty()) return null;
    List<AddressTemplateLine> filteredList =
        addressTemplateLineList.stream()
            .filter(
                addressTemplateLine ->
                    fieldName.equals(addressTemplateLine.getMetaField().getName()))
            .collect(Collectors.toList());

    if (filteredList.isEmpty()) return null;
    return filteredList.get(0);
  }
}
