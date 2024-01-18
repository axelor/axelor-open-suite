package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.CurrencyConversionLine;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CurrencyService {

  BigDecimal getCurrencyConversionRate(Currency startCurrency, Currency endCurrency)
      throws AxelorException;

  BigDecimal getCurrencyConversionRate(Currency startCurrency, Currency endCurrency, LocalDate date)
      throws AxelorException;

  BigDecimal getAmountCurrencyConvertedAtDate(
      Currency startCurrency, Currency endCurrency, BigDecimal amount, LocalDate date)
      throws AxelorException;

  BigDecimal getAmountCurrencyConvertedUsingExchangeRate(
      BigDecimal amount, BigDecimal exchangeRate, Currency endCurrency);

  LocalDate getDateToConvert(LocalDate date);

  void checkOverLappingPeriod(
      CurrencyConversionLine currentCcl, List<CurrencyConversionLine> currencyConversionLines)
      throws AxelorException;
}
