/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import jakarta.persistence.PersistenceException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

public class PaymentScheduleAccountRepository extends PaymentScheduleRepository {

  @Override
  public PaymentSchedule save(PaymentSchedule paymentSchedule) {
    try {
      if (Strings.isNullOrEmpty(paymentSchedule.getPaymentScheduleSeq())) {
        String num =
            Beans.get(SequenceService.class)
                .getSequenceNumber(
                    SequenceRepository.PAYMENT_SCHEDULE,
                    paymentSchedule.getCompany(),
                    PaymentSchedule.class,
                    "paymentScheduleSeq",
                    paymentSchedule);
        if (Strings.isNullOrEmpty(num) && ObjectUtils.notEmpty(paymentSchedule.getCompany())) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              String.format(
                  I18n.get(AccountExceptionMessage.PAYMENT_SCHEDULE_5),
                  paymentSchedule.getCompany().getName()));
        } else {
          paymentSchedule.setPaymentScheduleSeq(num);
        }
      }
    } catch (AxelorException e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
    return super.save(paymentSchedule);
  }

  @Override
  public PaymentSchedule copy(PaymentSchedule paymentSchedule, boolean deep) {
    PaymentSchedule copy = super.copy(paymentSchedule, deep);

    copy.setStatusSelect(PaymentScheduleRepository.STATUS_DRAFT);
    copy.setInvoiceSet(Collections.emptySet());
    copy.setPaymentScheduleLineList(Lists.newArrayList());
    copy.setPaymentScheduleSeq(null);
    LocalDate currentDate = LocalDate.now();
    copy.setCreationDate(currentDate);
    copy.setStartDate(currentDate);

    if (!copy.getTypeSelect().equals(PaymentScheduleRepository.TYPE_MONTHLY)) {
      copy.setInTaxAmount(BigDecimal.ZERO);
    }

    return copy;
  }
}
