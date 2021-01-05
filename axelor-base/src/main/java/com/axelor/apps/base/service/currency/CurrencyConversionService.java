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

import com.axelor.apps.base.db.AppBase;
import com.axelor.apps.base.db.Currency;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.time.LocalDate;
import wslite.http.HTTPResponse;
import wslite.json.JSONException;

public interface CurrencyConversionService {

  public void updateCurrencyConverion() throws AxelorException;

  public BigDecimal convert(Currency currencyFrom, Currency currencyTo)
      throws MalformedURLException, JSONException, AxelorException;

  public Float validateAndGetRate(
      int dayCount, Currency currencyFrom, Currency currencyTo, LocalDate date)
      throws AxelorException;

  public Float getRateFromJson(Currency currencyFrom, Currency currencyTo, HTTPResponse response)
      throws AxelorException;

  public String getVariations(BigDecimal currentRate, BigDecimal previousRate);

  public void createCurrencyConversionLine(
      Currency currencyFrom,
      Currency currencyTo,
      LocalDate fromDate,
      BigDecimal rate,
      AppBase appBase,
      String variations);
}
