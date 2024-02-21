package com.axelor.apps.base.web;

import com.axelor.apps.base.service.address.AddressTemplateLineViewService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AddressTemplateLineController {

  public void setDomainForMetaField(ActionRequest request, ActionResponse response) {
    response.setAttr(
        "metaField",
        "domain",
        Beans.get(AddressTemplateLineViewService.class).computeMetaFieldDomain());
  }
}
