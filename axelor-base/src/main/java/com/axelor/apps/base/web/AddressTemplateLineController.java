package com.axelor.apps.base.web;

import com.axelor.apps.base.db.AddressTemplateLine;
import com.axelor.apps.base.service.AddressTemplateLineService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import java.util.Map;

public class AddressTemplateLineController {
  private final AddressTemplateLineService addressTemplateLineService;

  @Inject
  public AddressTemplateLineController(AddressTemplateLineService addressTemplateLineService) {
    this.addressTemplateLineService = addressTemplateLineService;
  }

  public void setDomainForMetaField(ActionRequest request, ActionResponse response) {
    AddressTemplateLine addressTemplateLine =
        request.getContext().asType(AddressTemplateLine.class);

    String field = "metaField";
    Map<String, Map<String, Object>> addressTemplateFieldsForMetaFieldAttrsMap =
        addressTemplateLineService.getAddressTemplateFieldsForMetaField(field);
    response.setAttrs(addressTemplateFieldsForMetaFieldAttrsMap);
  }
}
