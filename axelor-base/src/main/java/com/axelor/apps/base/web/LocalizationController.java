package com.axelor.apps.base.web;

import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Localization;
import com.axelor.apps.base.service.LocalizationService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class LocalizationController {
  public void validateLocale(ActionRequest request, ActionResponse response) {
    try {
      Localization localization = request.getContext().asType(Localization.class);
      Beans.get(LocalizationService.class).validateLocale(localization);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void computeLocalePattern(ActionRequest request, ActionResponse response) {
    Localization localization = request.getContext().asType(Localization.class);
    String numberFormat =
        Beans.get(LocalizationService.class).getNumberFormat(localization.getCode());
    response.setValue("numbersFormat", numberFormat);
  }
}
