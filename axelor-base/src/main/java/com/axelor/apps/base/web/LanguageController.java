package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Language;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class LanguageController {
  public void convertToLowercase(ActionRequest request, ActionResponse response) {
    Language language = request.getContext().asType(Language.class);
    if (language.getCode() != null) {
      response.setValue("code", language.getCode().toLowerCase());
    }
  }
}
