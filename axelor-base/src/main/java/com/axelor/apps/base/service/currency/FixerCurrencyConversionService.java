/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.repo.CurrencyConversionLineRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wslite.http.HTTPResponse;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class FixerCurrencyConversionService extends ECBCurrencyConversionService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String FIXER_URL =
      "http://data.fixer.io/api/%s?access_key=%s&base=%s&symbols=%s";
  private final String key = appBaseService.getAppBase().getFixerApiKey();

  @Inject
  public FixerCurrencyConversionService(
      AppBaseService appBaseService, CurrencyConversionLineRepository cclRepo) {
    super(appBaseService, cclRepo);
  }

  @Override
  public BigDecimal convert(Currency currencyFrom, Currency currencyTo)
      throws MalformedURLException, JSONException, AxelorException {
    BigDecimal rate = new BigDecimal(-1);

    LOG.trace("Currerncy conversion From: {} To: {}", currencyFrom, currencyTo);
    LocalDate date =
        appBaseService.getTodayDate(
            Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null));
    if (currencyFrom != null && currencyTo != null) {
      Float rt = this.validateAndGetRate(1, currencyFrom, currencyTo, date);
      if (rt == null) {
        rt = 1.0f / this.validateAndGetRate(1, currencyTo, currencyFrom, date); // reverse
      }
      rate = BigDecimal.valueOf(rt).setScale(8, RoundingMode.HALF_EVEN);
    } else LOG.trace("Currency from and to must be filled to get rate");
    LOG.trace("Currerncy conversion rate: {}", rate);
    return rate;
  }

  @Override
  public Float getRateFromJson(Currency currencyFrom, Currency currencyTo, HTTPResponse response)
      throws AxelorException {
    try {
      JSONObject jsonResult = new JSONObject(response.getContentAsString());

      if (jsonResult.containsKey("rates")) {
        String rate = jsonResult.getJSONObject("rates").optString(currencyTo.getCode());
        return Float.parseFloat((rate));

      } else if (jsonResult.containsKey("error") && currencyTo.getCode().equals("EUR")) {
        int code = jsonResult.getJSONObject("error").getInt("code");
        if (code == 105 || code == 201) {
          return null;
        }
      }
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, response.getContentAsString());
    } catch (JSONException | NumberFormatException e) {
      throw new AxelorException(
          e.getCause(), TraceBackRepository.CATEGORY_INCONSISTENCY, e.getLocalizedMessage());
    }
  }

  @Override
  protected String getUrlString(LocalDate date, String currencyFromCode, String currencyToCode) {
    String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    return String.format(FIXER_URL, dateStr, key, currencyFromCode, currencyToCode);
  }
}
