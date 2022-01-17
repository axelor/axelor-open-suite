package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.PaymentSessionService;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Map;

public class PaymentSessionController {
  public void validateInvoiceTerms(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());

      if (Beans.get(PaymentSessionService.class).validateInvoiceTerms(paymentSession)) {
        response.setAlert(I18n.get(IExceptionMessage.PAYMENT_SESSION_INVALID_INVOICE_TERMS));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validatePaymentSession(ActionRequest request, ActionResponse response) {
    try {
      PaymentSession paymentSession = request.getContext().asType(PaymentSession.class);
      paymentSession = Beans.get(PaymentSessionRepository.class).find(paymentSession.getId());

      Map<Partner, Move> moveMap =
          Beans.get(PaymentSessionService.class).processPaymentSession(paymentSession);

      response.setFlash(
          String.format(
              I18n.get(IExceptionMessage.PAYMENT_SESSION_GENERATED_MOVES),
              moveMap.values().size()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
