package com.axelor.apps.account.service.payment;

import com.axelor.apps.account.db.InterestRateHistoryLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.AxelorException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentModeInterestRateService {
  void saveInterestRateToHistory(PaymentMode paymentMode) throws AxelorException;

  List<String> checkPeriodOverlap(
      PaymentMode paymentMode,
      InterestRateHistoryLine interestRateHistoryLine,
      Optional<LocalDate> fromDate,
      Optional<LocalDate> endDate);

  List<String> checkPeriodIsContinuous(
      PaymentMode paymentMode, Optional<LocalDate> fromDate, Optional<LocalDate> endDate);

  boolean checkEndDateIsInPast(Optional<LocalDate> endDate);
}
