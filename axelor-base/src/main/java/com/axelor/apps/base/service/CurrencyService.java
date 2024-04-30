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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.db.repo.CurrencyConversionLineRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.meta.CallMethod;
import com.axelor.utils.date.DateTool;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CurrencyService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppBaseService appBaseService;

  protected CurrencyConversionLineRepository currencyConversionLineRepo;

  private LocalDate today;

  @Inject
  public CurrencyService(
      AppBaseService appBaseService, CurrencyConversionLineRepository currencyConversionLineRepo) {

    this.appBaseService = appBaseService;
    this.today =
        appBaseService.getTodayDate(
            Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null));
    this.currencyConversionLineRepo = currencyConversionLineRepo;
  }

  public CurrencyService(AppBaseService appBaseService, LocalDate today) {

    this.appBaseService = appBaseService;
    this.today = today;
  }

  @CallMethod
  public BigDecimal getCurrencyConversionRate(Currency startCurrency, Currency endCurrency)
      throws AxelorException {
    return this.getCurrencyConversionRate(startCurrency, endCurrency, this.today);
  }

  public BigDecimal getCurrencyConversionRate(
      Currency startCurrency, Currency endCurrency, LocalDate date) throws AxelorException {

    // If the start currency is different from end currency
    // So we convert the amount
    if (startCurrency != null && endCurrency != null && !startCurrency.equals(endCurrency)) {

      LocalDate dateToConvert = this.getDateToConvert(date);
      boolean isInverse = true;
      BigDecimal exchangeRate = null;

      CurrencyConversionLine currencyConversionLine =
          this.getCurrencyConversionLine(startCurrency, endCurrency, dateToConvert);
      if (currencyConversionLine != null) {
        exchangeRate = currencyConversionLine.getExchangeRate();
        isInverse = false;

      } else {
        currencyConversionLine =
            this.getCurrencyConversionLine(endCurrency, startCurrency, dateToConvert);

        if (currencyConversionLine == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(BaseExceptionMessage.CURRENCY_1),
              startCurrency.getName(),
              endCurrency.getName(),
              dateToConvert);
        }
        exchangeRate = currencyConversionLine.getExchangeRate();
      }

      if (exchangeRate == null || exchangeRate.compareTo(BigDecimal.ZERO) == 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.CURRENCY_2),
            startCurrency.getName(),
            endCurrency.getName(),
            dateToConvert);
      }

      return isInverse
          ? BigDecimal.ONE.divide(
              exchangeRate, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP)
          : exchangeRate;
    }

    return BigDecimal.ONE;
  }

  protected CurrencyConversionLine getCurrencyConversionLine(
      Currency startCurrency, Currency endCurrency, LocalDate localDate) {

    List<CurrencyConversionLine> currencyConversionLineList =
        appBaseService.getCurrencyConfigurationLineList();

    if (currencyConversionLineList == null) {
      return null;
    }

    log.debug(
        "Currency from: {}, Currency to: {}, localDate: {}", startCurrency, endCurrency, localDate);

    for (CurrencyConversionLine ccl : currencyConversionLineList) {

      String cclStartCode = ccl.getStartCurrency().getCodeISO();
      String cclEndCode = ccl.getEndCurrency().getCodeISO();
      String startCode = startCurrency.getCodeISO();
      String endCode = endCurrency.getCodeISO();
      LocalDate fromDate = ccl.getFromDate();
      LocalDate toDate = ccl.getToDate();

      if (cclStartCode.equals(startCode) && cclEndCode.equals(endCode)) {
        if ((fromDate.isBefore(localDate) || fromDate.equals(localDate))
            && (toDate == null || toDate.isAfter(localDate) || toDate.isEqual(localDate))) {
          return ccl;
        }
      }
    }

    return null;
  }

  /**
   * Convert the amount in start currency into the end currency according to the date to convert
   *
   * @param startCurrency
   * @param endCurrency
   * @param amount
   * @param date
   * @return
   * @throws AxelorException
   */
  public BigDecimal getAmountCurrencyConvertedAtDate(
      Currency startCurrency, Currency endCurrency, BigDecimal amount, LocalDate date)
      throws AxelorException {

    // If the start currency is different from end currency
    // So we convert the amount
    if (startCurrency != null && endCurrency != null && !startCurrency.equals(endCurrency)) {

      return this.getAmountCurrencyConvertedUsingExchangeRate(
          amount, this.getCurrencyConversionRate(startCurrency, endCurrency, date));
    }

    return amount;
  }

  /**
   * Convert the amount in start currency into the end currency according to the exchange rate
   *
   * @param startCurrency
   * @param endCurrency
   * @param amount
   * @param exchangeRate
   * @return
   * @throws AxelorException
   */
  public BigDecimal getAmountCurrencyConvertedUsingExchangeRate(
      BigDecimal amount, BigDecimal exchangeRate) throws AxelorException {

    // If the start currency is different from end currency
    // So we convert the amount
    if (exchangeRate.compareTo(BigDecimal.ONE) != 0) {

      return amount
          .multiply(exchangeRate)
          .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
    }

    return amount;
  }

  public LocalDate getDateToConvert(LocalDate date) {

    if (date == null) {
      date = this.today;
    }

    return date;
  }

  public void checkOverLappingPeriod(
      CurrencyConversionLine currentCcl, List<CurrencyConversionLine> currencyConversionLines)
      throws AxelorException {

    LocalDate fromDate = currentCcl.getFromDate();
    LocalDate toDate = currentCcl.getToDate();
    Currency startCurrency = currentCcl.getStartCurrency();
    Currency endCurrency = currentCcl.getEndCurrency();

    if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.CURRENCY_4));
    }

    for (CurrencyConversionLine existingCcl : currencyConversionLines) {
      if (existingCcl.equals(currentCcl)
          || !(existingCcl.getStartCurrency().equals(startCurrency)
              && existingCcl.getEndCurrency().equals(endCurrency))) {
        continue;
      }

      LocalDate existingFromDate = existingCcl.getFromDate();
      LocalDate existingToDate = existingCcl.getToDate();

      if (existingToDate == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.CURRENCY_3),
            startCurrency.getCodeISO(),
            endCurrency.getCodeISO(),
            existingFromDate);
      } else if (DateTool.isBetween(existingFromDate, existingToDate, fromDate)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.CURRENCY_11),
            startCurrency.getCodeISO(),
            endCurrency.getCodeISO());
      }
    }
  }
}
