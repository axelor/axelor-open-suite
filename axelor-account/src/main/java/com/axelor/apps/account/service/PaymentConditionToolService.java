package com.axelor.apps.account.service;

import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentConditionLine;
import com.axelor.apps.account.db.repo.PaymentConditionLineRepository;
import com.axelor.common.ObjectUtils;
import java.time.LocalDate;
import java.util.Comparator;

public class PaymentConditionToolService {

  public static LocalDate getMaxDueDate(PaymentCondition paymentCondition, LocalDate defaultDate) {
    if (paymentCondition == null
        || ObjectUtils.isEmpty(paymentCondition.getPaymentConditionLineList())) {
      return defaultDate;
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
}
