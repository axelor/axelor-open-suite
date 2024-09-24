package com.axelor.apps.account.web;

import com.axelor.apps.account.db.InterestRateHistoryLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.payment.PaymentModeInterestRateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.time.LocalDate;
import java.util.Optional;

public class InterestRateHistoryLineController {

  public void checkPeriodConsistency(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    InterestRateHistoryLine interestRateHistoryLine = context.asType(InterestRateHistoryLine.class);
    PaymentMode paymentMode = interestRateHistoryLine.getPaymentMode();
    if (paymentMode == null) {
      paymentMode = context.getParent().asType(PaymentMode.class);
    }

    Optional<LocalDate> fromDate =
        Optional.ofNullable(context.get("fromDate")).map(Object::toString).map(LocalDate::parse);
    Optional<LocalDate> endDate =
        Optional.ofNullable(context.get("endDate")).map(Object::toString).map(LocalDate::parse);

    Beans.get(PaymentModeInterestRateService.class)
        .checkPeriodConsistency(paymentMode, interestRateHistoryLine, fromDate, endDate);
  }
}
