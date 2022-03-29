package com.axelor.apps.account.web;

import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class SequenceAccountController {

  public void checkIfFixedAssetSequenceExist(ActionRequest request, ActionResponse response) {
    try {
      Sequence sequence = request.getContext().asType(Sequence.class);
      if (sequence.getCompany() != null
          && Beans.get(SequenceService.class)
              .hasSequence(SequenceRepository.FIXED_ASSET, sequence.getCompany())) {
        response.setError(I18n.get(IExceptionMessage.FIXED_ASSET_SEQUENCE_ALREADY_EXISTS));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
