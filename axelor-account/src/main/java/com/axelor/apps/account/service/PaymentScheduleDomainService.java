package com.axelor.apps.account.service;

import com.axelor.apps.account.db.PaymentSchedule;

public interface PaymentScheduleDomainService {
  String createDomainForBankDetails(PaymentSchedule paymentSchedule);
}
