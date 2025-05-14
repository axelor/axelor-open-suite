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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentConditionLine;
import com.axelor.apps.account.db.repo.PaymentConditionLineRepository;
import com.axelor.common.ObjectUtils;
import java.time.LocalDate;
import java.util.Comparator;

public class PaymentConditionToolService {

  public static LocalDate getMaxDueDate(
      PaymentCondition paymentCondition, LocalDate defaultDate, LocalDate dueDate) {
    if (paymentCondition == null
        || ObjectUtils.isEmpty(paymentCondition.getPaymentConditionLineList())) {
      return defaultDate;
    }

    if (paymentCondition.getIsFree() && dueDate != null) {
      return dueDate;
    }

    return getDueDate(
        paymentCondition.getPaymentConditionLineList().stream()
            .max(Comparator.comparing(PaymentConditionLine::getSequence))
            .get(),
        defaultDate);
  }

  /**
   * Method to compute due date based on paymentConditionLine and date
   *
   * @param paymentConditionLine
   * @param date
   * @return
   */
  public static LocalDate getDueDate(PaymentConditionLine paymentConditionLine, LocalDate date) {

    return getDueDate(
        paymentConditionLine.getTypeSelect(),
        paymentConditionLine.getPaymentTime(),
        paymentConditionLine.getPeriodTypeSelect(),
        paymentConditionLine.getDaySelect(),
        date);
  }

  /**
   * Method to compute due date based on paymentCondition and date
   *
   * @param typeSelect
   * @param paymentTime
   * @param periodTypeSelect
   * @param daySelect
   * @param date
   * @return
   */
  public static LocalDate getDueDate(
      Integer typeSelect,
      Integer paymentTime,
      Integer periodTypeSelect,
      Integer daySelect,
      LocalDate date) {
    if (date == null) {
      return null;
    }

    LocalDate nDaysDate;
    if (periodTypeSelect.equals(PaymentConditionLineRepository.PERIOD_TYPE_DAYS)) {
      nDaysDate = date.plusDays(paymentTime);
    } else {
      nDaysDate = date.plusMonths(paymentTime);
    }

    switch (typeSelect) {
      case PaymentConditionLineRepository.TYPE_NET:
        return nDaysDate;

      case PaymentConditionLineRepository.TYPE_END_OF_MONTH_N_DAYS:
        if (periodTypeSelect.equals(PaymentConditionLineRepository.PERIOD_TYPE_DAYS)) {
          return date.withDayOfMonth(date.lengthOfMonth()).plusDays(paymentTime);
        } else {
          return date.withDayOfMonth(date.lengthOfMonth()).plusMonths(paymentTime);
        }
      case PaymentConditionLineRepository.TYPE_N_DAYS_END_OF_MONTH:
        return nDaysDate.withDayOfMonth(nDaysDate.lengthOfMonth());

      case PaymentConditionLineRepository.TYPE_N_DAYS_END_OF_MONTH_AT:
        return nDaysDate.withDayOfMonth(nDaysDate.lengthOfMonth()).plusDays(daySelect);
      default:
        return date;
    }
  }

  public static boolean isFreePaymentCondition(Invoice invoice) {
    return invoice.getPaymentCondition() != null && invoice.getPaymentCondition().getIsFree();
  }
}
