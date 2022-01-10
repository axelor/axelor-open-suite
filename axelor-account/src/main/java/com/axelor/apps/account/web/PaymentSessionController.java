package com.axelor.apps.account.web;

import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.service.PaymentSessionService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PaymentSessionController {
  public void cancelPaymentSession(ActionRequest request, ActionResponse response) {
    PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
    paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());

    Beans.get(PaymentSessionService.class).cancelPaymentSession(paymentSession);

    response.setReload(true);
  }
}
