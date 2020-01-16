/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.exception.AxelorException;
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
