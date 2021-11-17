package com.axelor.apps.account.web;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.PaymentModeControlService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PaymentModeController {

  public void setReadOnly(ActionRequest request, ActionResponse response) {

    try {
      PaymentMode paymentMode =
          Beans.get(PaymentModeRepository.class)
              .find(request.getContext().asType(PaymentMode.class).getId());
      if (paymentMode != null) {
        Boolean isInMove = Beans.get(PaymentModeControlService.class).isInMove(paymentMode);
        response.setAttr("name", "readonly", isInMove);
        response.setAttr("code", "readonly", isInMove);
        response.setAttr("typeSelect", "readonly", isInMove);
        response.setAttr("inOutSelect", "readonly", isInMove);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
