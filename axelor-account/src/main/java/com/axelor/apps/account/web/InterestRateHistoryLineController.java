package com.axelor.apps.account.web;

import com.axelor.apps.account.db.InterestRateHistoryLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.payment.PaymentModeInterestRateService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class InterestRateHistoryLineController {

  public void checkPeriodOverlap(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    InterestRateHistoryLine interestRateHistoryLine = context.asType(InterestRateHistoryLine.class);
    PaymentMode paymentMode =
        Optional.ofNullable(interestRateHistoryLine.getPaymentMode())
            .orElse(context.getParent().asType(PaymentMode.class));

    Optional<LocalDate> fromDate =
        Optional.ofNullable(context.get("fromDate")).map(Object::toString).map(LocalDate::parse);
    Optional<LocalDate> endDate =
        Optional.ofNullable(context.get("endDate")).map(Object::toString).map(LocalDate::parse);

    List<String> fieldsInError =
        Beans.get(PaymentModeInterestRateService.class)
            .checkPeriodOverlap(paymentMode, interestRateHistoryLine, fromDate, endDate);

    if (!fieldsInError.isEmpty()) {
      fieldsInError.forEach(field -> response.setValue(field, null));
      response.setError(
          I18n.get(AccountExceptionMessage.LATE_PAYMENT_INTEREST_HISTORY_PERIOD_OVERLAP));
    }
  }

  public void checkPeriodIsContinuous(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    InterestRateHistoryLine interestRateHistoryLine = context.asType(InterestRateHistoryLine.class);
    PaymentMode paymentMode =
        Optional.ofNullable(interestRateHistoryLine.getPaymentMode())
            .orElse(context.getParent().asType(PaymentMode.class));

    Optional<LocalDate> fromDate =
        Optional.ofNullable(context.get("fromDate")).map(Object::toString).map(LocalDate::parse);
    Optional<LocalDate> endDate =
        Optional.ofNullable(context.get("endDate")).map(Object::toString).map(LocalDate::parse);

    List<String> fieldsInError =
        Beans.get(PaymentModeInterestRateService.class)
            .checkPeriodIsContinuous(paymentMode, fromDate, endDate);

    if (!fieldsInError.isEmpty()) {
      fieldsInError.forEach(field -> response.setValue(field, null));
      response.setError(
          I18n.get(AccountExceptionMessage.LATE_PAYMENT_INTEREST_HISTORY_PERIOD_CONTINUITY));
    }
  }

  public void checkEndDateIsInPast(ActionRequest request, ActionResponse response) {
    Optional<LocalDate> endDate =
        Optional.ofNullable(request.getContext().get("endDate"))
            .map(Object::toString)
            .map(LocalDate::parse);

    if (Beans.get(PaymentModeInterestRateService.class).checkEndDateIsInPast(endDate)) {
      response.setValue("endDate", null);
      response.setError(
          I18n.get(AccountExceptionMessage.LATE_PAYMENT_INTEREST_HISTORY_END_DATE_IN_FUTURE));
    }
  }
}
