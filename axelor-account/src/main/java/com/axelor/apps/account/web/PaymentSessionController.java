package com.axelor.apps.account.web;

import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.PaymentSessionService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PaymentSessionController {
  public void validateInvoiceTerms(ActionRequest request, ActionResponse response) {
    PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
    paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());

    if (!Beans.get(PaymentSessionService.class).validateInvoiceTerms(paymentSession)) {
      response.setAlert(I18n.get(IExceptionMessage.PAYMENT_SESSION_INVALID_INVOICE_TERMS));
    }
  }

  public void validatePaymentSession(ActionRequest request, ActionResponse response) {
    PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
    paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());

    Beans.get(PaymentSessionService.class).processPaymentSession(paymentSession);
  }
}
