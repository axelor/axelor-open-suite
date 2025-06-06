package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.base.db.BankDetails;

public interface PaymentScheduleServiceBankPayment {
  BankDetails checkPaymentScheduleBankDetails(PaymentSchedule paymentSchedule);
}
