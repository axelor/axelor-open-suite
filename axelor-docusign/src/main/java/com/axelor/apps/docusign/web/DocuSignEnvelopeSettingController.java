package com.axelor.apps.docusign.web;

import com.axelor.apps.docusign.service.DocuSignEnvelopeSettingService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class DocuSignEnvelopeSettingController {

  @SuppressWarnings("unchecked")
  public void addItemToReferenceSelection(ActionRequest request, ActionResponse response) {
    try {
      MetaModel metaModel = (MetaModel) request.getContext().get("metaModel");
      if (metaModel == null) {
        return;
      }

      metaModel = Beans.get(MetaModelRepository.class).find(metaModel.getId());
      Beans.get(DocuSignEnvelopeSettingService.class).addItemToReferenceSelection(metaModel);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
