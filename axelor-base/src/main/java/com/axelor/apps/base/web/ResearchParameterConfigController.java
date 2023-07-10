package com.axelor.apps.base.web;

import com.axelor.apps.base.db.ResearchParameterConfig;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ResearchParameterConfigController {

  public void setMetaModel(ActionRequest request, ActionResponse response) {
    try {
      ResearchParameterConfig researchParameterConfig =
          request.getContext().asType(ResearchParameterConfig.class);
      String modelFullName = researchParameterConfig.getModel();
      if (modelFullName != null && modelFullName.contains("Contact")) {
        modelFullName = modelFullName.replace(".Contact", "");
      }

      MetaModel metaModel =
          Beans.get(MetaModelRepository.class)
              .all()
              .filter("self.fullName = ?1", modelFullName)
              .fetchOne();

      response.setValue("metaModel", metaModel);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }
}
