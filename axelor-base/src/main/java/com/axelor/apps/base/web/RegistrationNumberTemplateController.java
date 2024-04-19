package com.axelor.apps.base.web;

import com.axelor.apps.base.db.RegistrationNumberTemplate;
import com.axelor.apps.base.service.partner.registrationnumber.RegistrationNumberTemplateService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class RegistrationNumberTemplateController {
  public void checkParameters(ActionRequest request, ActionResponse response) {
    RegistrationNumberTemplate registrationNumberTemplate =
        request.getContext().asType(RegistrationNumberTemplate.class);
    String errorMessage =
        Beans.get(RegistrationNumberTemplateService.class)
            .checkParametersLegality(registrationNumberTemplate);
    if (errorMessage != null) {
      response.setError(errorMessage);
    }
  }
}
