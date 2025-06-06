package com.axelor.apps.bankpayment.web;

import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.bankpayment.service.PaymentScheduleServiceBankPayment;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PaymentScheduleController {

  @ErrorException
  public void checkPaymentScheduleBankDetails(ActionRequest request, ActionResponse response) {
    PaymentSchedule paymentSchedule = request.getContext().asType(PaymentSchedule.class);
    BankDetails bankDetails =
        Beans.get(PaymentScheduleServiceBankPayment.class)
            .checkPaymentScheduleBankDetails(paymentSchedule);
    response.setValue("bankDetails", bankDetails);
  }
}
