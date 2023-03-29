package com.axelor.apps.base.web;

import com.axelor.apps.base.db.FakerApiField;
import com.axelor.apps.base.service.FakerService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Objects;

public class FakerApiFieldController {

  public void checkMethod(ActionRequest request, ActionResponse response) {
    FakerApiField fakerApiField = request.getContext().asType(FakerApiField.class);

    if (Objects.isNull(fakerApiField.getClassName())) {
      response.setError(I18n.get("The fields are empty."));
    } else {
      Beans.get(FakerService.class).checkMethod(fakerApiField, response);
    }
  }
}
