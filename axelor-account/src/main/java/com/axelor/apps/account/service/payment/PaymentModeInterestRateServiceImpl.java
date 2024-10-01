package com.axelor.apps.account.service.payment;

import com.axelor.apps.account.db.InterestRateHistoryLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InterestRateHistoryLineRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.date.LocalDateHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PaymentModeInterestRateServiceImpl implements PaymentModeInterestRateService {

  protected AppAccountService appAccountService;
  protected InterestRateHistoryLineRepository interestRateHistoryLineRepository;

  @Inject
  public PaymentModeInterestRateServiceImpl(
      AppAccountService appAccountService,
      InterestRateHistoryLineRepository interestRateHistoryLineRepository) {
    this.appAccountService = appAccountService;
    this.interestRateHistoryLineRepository = interestRateHistoryLineRepository;
  }

  /**
   * Save current rate into history
   *
   * @param paymentMode
   * @throws AxelorException
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public void saveInterestRateToHistory(PaymentMode paymentMode) throws AxelorException {
    // check if the rate is filled
    BigDecimal interestRate = paymentMode.getInterestRate();
    if (interestRate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.LATE_PAYMENT_INTEREST_NO_INTEREST_RATE));
    }

    LocalDate todayDate =
        appAccountService.getTodayDate(
            Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null));
    LocalDate yesterday = todayDate.minusDays(1);

    LocalDate fromDate;
    if (paymentMode.getInterestRateHistoryLineList().isEmpty()) {
      fromDate = yesterday;
    } else {
      InterestRateHistoryLine lastInterestRateHistoryLine =
          getLastInterestRateHistoryLine(paymentMode);
      fromDate = lastInterestRateHistoryLine.getEndDate().plusDays(1);
    }

    // yesterday will be end date so it must be after fromDate
    if (fromDate.isAfter(yesterday)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.LATE_PAYMENT_INTEREST_HISTORY_DATES_INCONSISTENCY));
    }

    InterestRateHistoryLine interestRateHistoryLine = new InterestRateHistoryLine();
    interestRateHistoryLine.setInterestRate(interestRate);
    interestRateHistoryLine.setPaymentMode(paymentMode);
    interestRateHistoryLine.setFromDate(fromDate);
    interestRateHistoryLine.setEndDate(yesterday);

    paymentMode.addInterestRateHistoryLineListItem(interestRateHistoryLine);

    interestRateHistoryLineRepository.save(interestRateHistoryLine);
  }

  protected InterestRateHistoryLine getLastInterestRateHistoryLine(PaymentMode paymentMode) {
    return Collections.max(
        paymentMode.getInterestRateHistoryLineList(),
        Comparator.comparing(InterestRateHistoryLine::getFromDate));
  }

  @Override
  public List<String> checkPeriodOverlap(
      PaymentMode paymentMode,
      InterestRateHistoryLine interestRateHistoryLine,
      LocalDate fromDate,
      LocalDate endDate) {

    List<String> fieldsInError = new ArrayList<>();

    List<InterestRateHistoryLine> interestRateHistoryLineList =
        paymentMode.getInterestRateHistoryLineList().stream()
            .filter(line1 -> line1.getId() != interestRateHistoryLine.getId())
            .collect(Collectors.toList());

    // if no element, no need to check
    if (interestRateHistoryLineList.isEmpty()) {
      return fieldsInError;
    }

    for (InterestRateHistoryLine rateHistoryLine : interestRateHistoryLineList) {

      if (checkDateIsInPeriod(
          fromDate, rateHistoryLine.getFromDate(), rateHistoryLine.getEndDate())) {
        fieldsInError.add("fromDate");
      }
      if (checkDateIsInPeriod(
          endDate, rateHistoryLine.getFromDate(), rateHistoryLine.getEndDate())) {
        fieldsInError.add("endDate");
      }
    }
    // check if the from and end dates are not surrounding all periods
    LocalDate minFromDate =
        Collections.min(
                interestRateHistoryLineList,
                Comparator.comparing(InterestRateHistoryLine::getFromDate))
            .getFromDate();

    if (fromDate != null
        && endDate != null
        && LocalDateHelper.isBetween(fromDate, endDate, minFromDate)) {
      fieldsInError.add("fromDate");
      fieldsInError.add("endDate");
    }

    return fieldsInError;
  }

  @Override
  public List<String> checkPeriodIsContinuous(
      PaymentMode paymentMode,
      InterestRateHistoryLine interestRateHistoryLine,
      LocalDate fromDate,
      LocalDate endDate) {

    List<String> fieldsInError = new ArrayList<>();

    List<InterestRateHistoryLine> interestRateHistoryLineList =
        paymentMode.getInterestRateHistoryLineList().stream()
            .filter(
                historyLine ->
                    historyLine.getId() != null
                        && historyLine.getId() != interestRateHistoryLine.getId())
            .collect(Collectors.toList());
    if (interestRateHistoryLineList.isEmpty()) {
      return fieldsInError;
    }

    LocalDate minFromDate =
        Collections.min(
                interestRateHistoryLineList,
                Comparator.comparing(InterestRateHistoryLine::getFromDate))
            .getFromDate();

    LocalDate maxEndDate =
        Collections.max(
                interestRateHistoryLineList,
                Comparator.comparing(InterestRateHistoryLine::getEndDate))
            .getEndDate();

    boolean fromDateIsNotContinue = fromDate != null && !fromDate.isEqual(maxEndDate.plusDays(1));
    boolean endDateIsNotContinue = endDate != null && !endDate.isEqual(minFromDate.minusDays(1));

    // if dectecting non continuous dates
    if (fromDateIsNotContinue && endDateIsNotContinue) {

      // allow to find if it's the from or end date that is not continuous

      if (fromDate.isBefore(minFromDate) && endDate.isBefore(minFromDate)) {
        fieldsInError.add("endDate");
      }

      if (fromDate.isAfter(maxEndDate) && endDate.isAfter(maxEndDate)) {
        fieldsInError.add("fromDate");
      }
    }
    return fieldsInError;
  }

  protected boolean checkDateIsInPeriod(
      LocalDate dateToCheck, LocalDate fromDate, LocalDate endDate) {
    return dateToCheck != null
        && fromDate != null
        && endDate != null
        && LocalDateHelper.isBetween(fromDate, endDate, dateToCheck);
  }

  @Override
  public boolean checkEndDateIsInPast(LocalDate endDate) {
    return endDate != null
        && endDate.isAfter(
            appAccountService.getTodayDate(
                Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null)));
  }
}
