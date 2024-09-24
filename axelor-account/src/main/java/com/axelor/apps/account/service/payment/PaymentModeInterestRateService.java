package com.axelor.apps.account.service.payment;

import com.axelor.apps.account.db.InterestRateHistoryLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.AxelorException;
import java.time.LocalDate;
import java.util.Optional;

public interface PaymentModeInterestRateService {
  void saveInterestRateToHistory(PaymentMode paymentMode) throws AxelorException;

  void checkPeriodConsistency(
      PaymentMode paymentMode,
      InterestRateHistoryLine interestRateHistoryLine,
      Optional<LocalDate> fromDate,
      Optional<LocalDate> endDate)
      throws AxelorException;
}
