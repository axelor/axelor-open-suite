package com.axelor.apps.account.web;

import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.PaymentSessionService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PaymentSessionController {
  public void sendEmails(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());

      int emailCount = Beans.get(PaymentSessionService.class).sendEmails(paymentSession);

      response.setReload(true);

      if (emailCount == 0) {
        response.setFlash(I18n.get(IExceptionMessage.PAYMENT_SESSION_NO_EMAIL_SENT));
      } else {
        response.setFlash(
            String.format(I18n.get(IExceptionMessage.PAYMENT_SESSION_EMAIL_SENT), emailCount));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
