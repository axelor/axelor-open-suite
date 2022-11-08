package com.axelor.apps.account.service;

import com.axelor.apps.account.db.PaymentSession;

public interface PaymentSessionCancelService {
  public void cancelPaymentSession(PaymentSession paymentSession);
}
