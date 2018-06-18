package com.axelor.apps.businessproject.web;

import com.axelor.apps.businessproject.db.ProductTaskTemplate;
import com.axelor.apps.businessproject.service.ProductTaskTemplateService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class ProductTaskTemplateController {

  public void removeTask(ActionRequest request, ActionResponse response) {
    try {
      ProductTaskTemplate template = request.getContext().asType(ProductTaskTemplate.class);
      Beans.get(ProductTaskTemplateService.class).remove(template);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
