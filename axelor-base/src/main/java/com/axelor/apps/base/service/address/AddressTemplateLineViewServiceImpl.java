package com.axelor.apps.base.service.address;

import com.google.inject.Inject;
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
}
