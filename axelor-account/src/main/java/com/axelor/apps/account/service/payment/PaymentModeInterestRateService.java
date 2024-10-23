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
package com.axelor.apps.account.service.payment;

import com.axelor.apps.account.db.InterestRateHistoryLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.AxelorException;
import java.time.LocalDate;
import java.util.List;

public interface PaymentModeInterestRateService {
  void saveInterestRateToHistory(PaymentMode paymentMode) throws AxelorException;

  List<String> checkPeriodOverlap(
      PaymentMode paymentMode,
      InterestRateHistoryLine interestRateHistoryLine,
      LocalDate fromDate,
      LocalDate endDate);

  List<String> checkPeriodIsContinuous(
      PaymentMode paymentMode,
      InterestRateHistoryLine interestRateHistoryLine,
      LocalDate fromDate,
      LocalDate endDate);

  boolean checkEndDateIsInPast(LocalDate endDate);
}
