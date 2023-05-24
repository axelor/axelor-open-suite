package com.axelor.apps.account.service;

import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.base.AxelorException;

public interface PaymentConditionService {
  void checkPaymentCondition(PaymentCondition paymentCondition) throws AxelorException;
}
