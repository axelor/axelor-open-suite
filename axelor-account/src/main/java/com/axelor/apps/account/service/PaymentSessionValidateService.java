package com.axelor.apps.account.service;

import com.axelor.apps.account.db.PaymentSession;
import com.axelor.exception.AxelorException;

public interface PaymentSessionValidateService {
  public boolean validateInvoiceTerms(PaymentSession paymentSession);

  public int processPaymentSession(PaymentSession paymentSession) throws AxelorException;

  public StringBuilder generateFlashMessage(PaymentSession paymentSession, int moveCount);
}
