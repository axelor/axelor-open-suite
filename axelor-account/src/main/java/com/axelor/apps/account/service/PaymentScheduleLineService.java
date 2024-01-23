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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PaymentScheduleLineService {

  PaymentScheduleLine createPaymentScheduleLine(
      PaymentSchedule paymentSchedule,
      BigDecimal inTaxAmount,
      int scheduleLineSeq,
      LocalDate scheduleDate);

  List<PaymentScheduleLine> createPaymentScheduleLines(PaymentSchedule paymentSchedule);

  /**
   * Create a payment move for a payment schedule line with the given company bank details.
   *
   * @param paymentScheduleLine
   * @param companyBankDetails
   * @param paymentMode
   * @return
   * @throws AxelorException
   */
  Move createPaymentMove(
      PaymentScheduleLine paymentScheduleLine,
      BankDetails companyBankDetails,
      PaymentMode paymentMode)
      throws AxelorException;
}
