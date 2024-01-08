package com.axelor.apps.base.service;

import com.google.inject.Inject;

import java.util.HashMap;
import java.util.Map;

public class AddressTemplateLineServiceImpl implements AddressTemplateLineService {

  private final AddressAttrsService addressAttrsService;

  @Inject
  public AddressTemplateLineServiceImpl(AddressAttrsService addressAttrsService) {
    this.addressAttrsService = addressAttrsService;
  }

  public Map<String, Map<String, Object>> getAddressTemplateFieldsForMetaField(String field) {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();
    addressAttrsService.addAddressDomainFields(field, attrsMap);
    return attrsMap;
  }
}
