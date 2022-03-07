/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.currency;

import com.axelor.apps.base.db.AppBase;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.db.repo.CurrencyConversionLineRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wslite.http.HTTPResponse;
import wslite.json.JSONException;

public abstract class CurrencyConversionService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppBaseService appBaseService;
  protected CurrencyConversionLineRepository cclRepo;

  @Inject
  public CurrencyConversionService(
      AppBaseService appBaseService, CurrencyConversionLineRepository cclRepo) {
    this.appBaseService = appBaseService;
    this.cclRepo = cclRepo;
  }

  /**
   * Create new CurrencyConversionLine with updated rate and variation between previous and new rate
   *
   * @throws AxelorException
   */
  public abstract void updateCurrencyConverion() throws AxelorException;

  /**
   * Getting rate of currencyFrom in terms of currencyTo.
   *
   * <p>Example: 1 EUR = x USD
   *
   * @param currencyFrom
   * @param currencyTo
   * @return
   * @throws MalformedURLException
   * @throws JSONException
   * @throws AxelorException
   */
  public abstract BigDecimal convert(Currency currencyFrom, Currency currencyTo)
      throws MalformedURLException, JSONException, AxelorException;

  /**
   * Validate the response and get the rate.
   *
   * @param dayCount
   * @param currencyFrom
   * @param currencyTo
   * @param date
   * @return
   * @throws AxelorException
   */
  public abstract Float validateAndGetRate(
      int dayCount, Currency currencyFrom, Currency currencyTo, LocalDate date)
      throws AxelorException;

  /**
   * Get the actual rate from the JSON body of the response.
   *
   * @param currencyFrom
   * @param currencyTo
   * @param response
   * @return
   * @throws AxelorException
   */
  public abstract Float getRateFromJson(
      Currency currencyFrom, Currency currencyTo, HTTPResponse response) throws AxelorException;

  /**
   * Returns the URL to which request is to be sent.
   *
   * @param date
   * @param currencyFromCode
   * @param currencyToCode
   * @return
   */
  public abstract String getUrlString(
      LocalDate date, String currencyFromCode, String currencyToCode);

  /**
   * Returns the variation between new rate and the previous rate.
   *
   * @param currentRate
   * @param previousRate
   * @return
   */
  public String getVariations(BigDecimal currentRate, BigDecimal previousRate) {
    String variations = "0";
    LOG.trace(
        "Currency rate variation calculation for CurrentRate: {} PreviousRate: {}",
        new Object[] {currentRate, previousRate});

    if (currentRate != null
        && previousRate != null
        && previousRate.compareTo(BigDecimal.ZERO) != 0) {
      BigDecimal diffRate = currentRate.subtract(previousRate);
      BigDecimal variation =
          diffRate.multiply(new BigDecimal(100)).divide(previousRate, RoundingMode.HALF_UP);
      variation =
          variation.setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
      variations = variation.toString() + "%";
    }

    LOG.trace("Currency rate variation result: {}", new Object[] {variations});
    return variations;
  }

  /**
   * Create new CurrencyConversionLine. (Transaction)
   *
   * @param currencyFrom
   * @param currencyTo
   * @param fromDate
   * @param rate
   * @param appBase
   * @param variations
   */
  @Transactional
  public void createCurrencyConversionLine(
      Currency currencyFrom,
      Currency currencyTo,
      LocalDate fromDate,
      BigDecimal rate,
      AppBase appBase,
      String variations) {
    LOG.trace(
        "Create new currency conversion line CurrencyFrom: {}, CurrencyTo: {},FromDate: {},ConversionRate: {}, AppBase: {}, Variations: {}",
        new Object[] {currencyFrom, currencyTo, fromDate, rate, appBase, variations});

    CurrencyConversionLine ccl = new CurrencyConversionLine();
    ccl.setStartCurrency(currencyFrom);
    ccl.setEndCurrency(currencyTo);
    ccl.setFromDate(fromDate);
    ccl.setExchangeRate(rate);
    ccl.setAppBase(appBase);
    ccl.setVariations(variations);
    cclRepo.save(ccl);
  }

  @Transactional
  public void saveCurrencyConversionLine(CurrencyConversionLine ccl) {
    cclRepo.save(ccl);
  }
}
