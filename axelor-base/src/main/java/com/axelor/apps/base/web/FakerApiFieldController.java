package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.FakerApiField;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.FakerService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Objects;

public class FakerApiFieldController {

  public void checkMethod(ActionRequest request, ActionResponse response) throws AxelorException {
    FakerApiField fakerApiField = request.getContext().asType(FakerApiField.class);

    if (Objects.isNull(fakerApiField.getClassName())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FAKER_FIELDS_EMPTY));
    }

    String exampleOutput = Beans.get(FakerService.class).checkMethod(fakerApiField);
    response.setAlert(
        String.format(I18n.get(BaseExceptionMessage.FAKER_METHOD_EXAMPLE_OUTPUT), exampleOutput));
  }
}
