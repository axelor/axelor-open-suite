/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.batch;

import com.axelor.apps.account.db.repo.PaymentScheduleRepository;
import com.axelor.exception.db.repo.ExceptionOriginRepository;
import com.axelor.exception.service.TraceBackService;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchDirectDebitMonthlyPaymentSchedule extends BatchDirectDebitPaymentSchedule {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  protected void process() {
    processPaymentScheduleLines(PaymentScheduleRepository.TYPE_MONTHLY);

    if (batchBankPaymentService.paymentScheduleLineDoneListExists(batch) && generateBankOrderFlag) {
      try {
        findBatch();
        batchBankPaymentService.createBankOrderFromMonthlyPaymentScheduleLines(batch);
      } catch (Exception e) {
        TraceBackService.trace(e, ExceptionOriginRepository.DIRECT_DEBIT, batch.getId());
        logger.error(e.getLocalizedMessage());
      }
    }
  }
}
