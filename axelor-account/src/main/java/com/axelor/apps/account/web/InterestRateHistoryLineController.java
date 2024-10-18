/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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

    LocalDate fromDate =
        Optional.ofNullable(context.get("fromDate"))
            .map(Object::toString)
            .map(LocalDate::parse)
            .orElse(null);
    LocalDate endDate =
        Optional.ofNullable(context.get("endDate"))
            .map(Object::toString)
            .map(LocalDate::parse)
            .orElse(null);

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

    LocalDate fromDate =
        Optional.ofNullable(context.get("fromDate"))
            .map(Object::toString)
            .map(LocalDate::parse)
            .orElse(null);
    LocalDate endDate =
        Optional.ofNullable(context.get("endDate"))
            .map(Object::toString)
            .map(LocalDate::parse)
            .orElse(null);

    List<String> fieldsInError =
        Beans.get(PaymentModeInterestRateService.class)
            .checkPeriodIsContinuous(paymentMode, interestRateHistoryLine, fromDate, endDate);

    if (!fieldsInError.isEmpty()) {
      fieldsInError.forEach(field -> response.setValue(field, null));
      response.setError(
          I18n.get(AccountExceptionMessage.LATE_PAYMENT_INTEREST_HISTORY_PERIOD_CONTINUITY));
    }
  }

  public void checkEndDateIsInPast(ActionRequest request, ActionResponse response) {
    LocalDate endDate =
        Optional.ofNullable(request.getContext().get("endDate"))
            .map(Object::toString)
            .map(LocalDate::parse)
            .orElse(null);

    if (Beans.get(PaymentModeInterestRateService.class).checkEndDateIsInPast(endDate)) {
      response.setValue("endDate", null);
      response.setError(
          I18n.get(AccountExceptionMessage.LATE_PAYMENT_INTEREST_HISTORY_END_DATE_IN_FUTURE));
    }
  }
}
