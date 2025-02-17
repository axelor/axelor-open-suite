package com.axelor.apps.quality.web;

import com.axelor.apps.base.service.TagService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.AuthUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class QualityAlertController {

  public void setQualityTagDomain(ActionRequest request, ActionResponse response) {
    try {
      response.setAttr(
          "tagSet",
          "domain",
          Beans.get(TagService.class)
              .getTagDomain("QualityAlert", AuthUtils.getUser().getActiveCompany()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
