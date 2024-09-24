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

  /**
   * Check period dates
   *
   * @param paymentMode
   * @param interestRateHistoryLine
   * @param fromDate
   * @param endDate
   * @throws AxelorException
   */
  @Override
  public void checkPeriodConsistency(
      PaymentMode paymentMode,
      InterestRateHistoryLine interestRateHistoryLine,
      Optional<LocalDate> fromDate,
      Optional<LocalDate> endDate)
      throws AxelorException {

    checkPeriodOverlap(paymentMode, interestRateHistoryLine, fromDate, endDate);
    List<InterestRateHistoryLine> interestRateHistoryLineList =
        paymentMode.getInterestRateHistoryLineList().stream()
            .filter(historyLine -> historyLine.getId() != null)
            .collect(Collectors.toList());
    if (!interestRateHistoryLineList.isEmpty()) {
      checkPeriodIsContinuous(interestRateHistoryLineList, fromDate, endDate);
    }
  }

  protected void checkPeriodOverlap(
      PaymentMode paymentMode,
      InterestRateHistoryLine interestRateHistoryLine,
      Optional<LocalDate> fromDate,
      Optional<LocalDate> endDate)
      throws AxelorException {
    for (InterestRateHistoryLine rateHistoryLine : paymentMode.getInterestRateHistoryLineList()) {
      if (rateHistoryLine.getId() == interestRateHistoryLine.getId()) {
        continue;
      }

      checkDateIsInPeriod(fromDate, rateHistoryLine.getFromDate(), rateHistoryLine.getEndDate());
      checkDateIsInPeriod(endDate, rateHistoryLine.getFromDate(), rateHistoryLine.getEndDate());
      checkEndDateIsInPast(endDate);
    }
  }

  protected void checkPeriodIsContinuous(
      List<InterestRateHistoryLine> interestRateHistoryLineList,
      Optional<LocalDate> fromDate,
      Optional<LocalDate> endDate)
      throws AxelorException {
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

    boolean fromDateIsNotContinue =
        fromDate.isPresent() && !fromDate.get().isEqual(maxEndDate.plusDays(1));
    boolean endDateIsNotContinue =
        endDate.isPresent() && !endDate.get().isEqual(minFromDate.minusDays(1));
    boolean endDateIsNotContinueWithNoFromDate =
        endDate.isPresent()
            && fromDate.isEmpty()
            && !endDate.get().isEqual(minFromDate.minusDays(1));
    boolean fromDateIsNotContinueWithNoEndDate =
        fromDate.isPresent()
            && endDate.isEmpty()
            && !fromDate.get().isEqual(maxEndDate.plusDays(1));

    if ((fromDateIsNotContinue && endDateIsNotContinue)
        || endDateIsNotContinueWithNoFromDate
        || fromDateIsNotContinueWithNoEndDate) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.LATE_PAYMENT_INTEREST_HISTORY_PERIOD_CONTINUITY));
    }
  }

  protected void checkDateIsInPeriod(
      Optional<LocalDate> dateToCheck, LocalDate fromDate, LocalDate endDate)
      throws AxelorException {
    if (dateToCheck.isPresent()
        && fromDate != null
        && endDate != null
        && LocalDateHelper.isBetween(fromDate, endDate, dateToCheck.get())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.LATE_PAYMENT_INTEREST_HISTORY_PERIOD_OVERLAP));
    }
  }

  protected void checkEndDateIsInPast(Optional<LocalDate> endDate) throws AxelorException {
    if (endDate.isPresent()
        && endDate
            .get()
            .isAfter(
                appAccountService.getTodayDate(
                    Optional.ofNullable(AuthUtils.getUser())
                        .map(User::getActiveCompany)
                        .orElse(null)))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.LATE_PAYMENT_INTEREST_HISTORY_END_DATE_IN_FUTURE));
    }
  }
}
