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
package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.db.repo.CurrencyConversionLineRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.currency.CurrencyConversionFactory;
import com.axelor.apps.base.service.currency.CurrencyConversionService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.studio.db.AppBase;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wslite.json.JSONException;

@Singleton
public class CurrencyConversionLineController {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void checkDate(ActionRequest request, ActionResponse response) {

    CurrencyConversionLine ccl = request.getContext().asType(CurrencyConversionLine.class);
    Context parentContext = request.getContext().getParent();

    if (parentContext != null && AppBase.class.equals(parentContext.getContextClass())) {
      AppBase appBase = parentContext.asType(AppBase.class);

      LOG.debug("Currency Conversion Line Id : {}", ccl.getId());

      try {
        Beans.get(CurrencyService.class)
            .checkOverLappingPeriod(ccl, appBase.getCurrencyConversionLineList());
      } catch (AxelorException e) {
        response.setInfo(e.getLocalizedMessage());
        response.setValue("fromDate", null);
        response.setValue("toDate", null);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void convert(ActionRequest request, ActionResponse response)
      throws MalformedURLException, JSONException, AxelorException {
    Context context = request.getContext();
    Currency fromCurrency = null;
    Currency toCurrency = null;
    CurrencyRepository currencyRepository = Beans.get(CurrencyRepository.class);
    BigDecimal rate = null;

    if (context.get("startCurrency") instanceof Currency) {
      fromCurrency = (Currency) context.get("startCurrency");
      toCurrency = (Currency) context.get("endCurrency");
    } else {
      Map<String, Object> startCurrency = (Map<String, Object>) context.get("startCurrency");
      Map<String, Object> endCurrency = (Map<String, Object>) context.get("endCurrency");

      fromCurrency = currencyRepository.find(Long.parseLong(startCurrency.get("id").toString()));
      toCurrency = currencyRepository.find(Long.parseLong(endCurrency.get("id").toString()));
    }

    CurrencyConversionLine prevLine = null;

    if (fromCurrency != null && toCurrency != null) {

      if (context.get("id") != null)
        prevLine =
            Beans.get(CurrencyConversionLineRepository.class)
                .all()
                .filter(
                    "startCurrency.id = ?1 AND endCurrency.id = ?2 AND id != ?3",
                    fromCurrency.getId(),
                    toCurrency.getId(),
                    context.get("id"))
                .order("-fromDate")
                .fetchOne();
      else
        prevLine =
            Beans.get(CurrencyConversionLineRepository.class)
                .all()
                .filter(
                    "startCurrency.id = ?1 AND endCurrency.id = ?2",
                    fromCurrency.getId(),
                    toCurrency.getId())
                .order("-fromDate")
                .fetchOne();

      LOG.debug("Previous currency conversion line: {}", prevLine);
      fromCurrency = currencyRepository.find(fromCurrency.getId());
      toCurrency = currencyRepository.find(toCurrency.getId());

      try {
        CurrencyConversionService currencyConversionService =
            Beans.get(CurrencyConversionFactory.class).getCurrencyConversionService();

        Pair<LocalDate, BigDecimal> pair =
            currencyConversionService.getRateWithDate(fromCurrency, toCurrency);
        rate = pair.getRight();
        if (rate.compareTo(new BigDecimal(-1)) == 0)
          response.setInfo(I18n.get(BaseExceptionMessage.CURRENCY_6));
        else {
          response.setValue("variations", "0");
          if (context.get("_model").equals("com.axelor.utils.db.Wizard"))
            response.setValue("newExchangeRate", rate);
          else response.setValue("exchangeRate", rate);
          LocalDate todayDate =
              Beans.get(AppBaseService.class)
                  .getTodayDate(
                      Optional.ofNullable(AuthUtils.getUser())
                          .map(User::getActiveCompany)
                          .orElse(null));
          LocalDate rateRetrieveDate = pair.getLeft();
          Context parentContext = request.getContext().getParent();

          if (parentContext != null && AppBase.class.equals(parentContext.getContextClass())) {
            AppBase appBase = parentContext.asType(AppBase.class);
            CurrencyConversionLine currencyConversionLine =
                context.asType(CurrencyConversionLine.class);
            currencyConversionLine.setFromDate(rateRetrieveDate);

            Beans.get(CurrencyService.class)
                .checkOverLappingPeriod(
                    currencyConversionLine, appBase.getCurrencyConversionLineList());
          }
          response.setValue("fromDate", rateRetrieveDate);
          if (prevLine != null)
            response.setValue(
                "variations",
                currencyConversionService.getVariations(rate, prevLine.getExchangeRate()));

          if (!rateRetrieveDate.equals(todayDate)) {
            response.setInfo(
                String.format(
                    I18n.get(BaseExceptionMessage.CURRENCY_10), todayDate, rateRetrieveDate));
          }
        }
      } catch (AxelorException axelorException) {
        response.setInfo(axelorException.getMessage());
        response.setValue("fromDate", null);
        response.setValue("toDate", null);
        response.setCanClose(true);
      }
    }
  }
}
