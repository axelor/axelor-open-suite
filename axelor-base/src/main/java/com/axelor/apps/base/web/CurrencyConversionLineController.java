/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.db.repo.CurrencyConversionLineRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.CurrencyConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wslite.json.JSONException;

@Singleton
public class CurrencyConversionLineController {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  @Inject private CurrencyConversionService ccs;

  @Inject private CurrencyConversionLineRepository cclRepo;

  @Inject private AppBaseService appBaseService;

  @Inject private CurrencyRepository currencyRepo;

  public void checkDate(ActionRequest request, ActionResponse response) {

    CurrencyConversionLine ccl = request.getContext().asType(CurrencyConversionLine.class);

    LOG.debug("Currency Conversion Line Id : {}", ccl.getId());

    if (ccl.getId() != null
        && cclRepo
                .all()
                .filter(
                    "self.startCurrency.id = ?1 and self.endCurrency.id = ?2 and (self.toDate = null OR  self.toDate >= ?3) and self.id != ?4)",
                    ccl.getStartCurrency().getId(),
                    ccl.getEndCurrency().getId(),
                    ccl.getFromDate(),
                    ccl.getId())
                .count()
            > 0) {
      response.setFlash(I18n.get(IExceptionMessage.CURRENCY_3));
      //			response.setValue("fromDate", "");
    } else if (ccl.getId() == null
        && cclRepo
                .all()
                .filter(
                    "self.startCurrency.id = ?1 and self.endCurrency.id = ?2 and (self.toDate = null OR  self.toDate >= ?3))",
                    ccl.getStartCurrency().getId(),
                    ccl.getEndCurrency().getId(),
                    ccl.getFromDate())
                .count()
            > 0) {
      response.setFlash(I18n.get(IExceptionMessage.CURRENCY_3));
      //			response.setValue("fromDate", "");
    }

    if (ccl.getFromDate() != null
        && ccl.getToDate() != null
        && ccl.getFromDate().isAfter(ccl.getToDate())) {
      response.setFlash(I18n.get(IExceptionMessage.CURRENCY_4));
      //			response.setValue("fromDate", "");
    }
  }

  @SuppressWarnings("unchecked")
  public void convert(ActionRequest request, ActionResponse response)
      throws MalformedURLException, JSONException, AxelorException {
    Context context = request.getContext();
    Currency fromCurrency = null;
    Currency toCurrency = null;

    if (context.get("startCurrency") instanceof Currency) {
      fromCurrency = (Currency) context.get("startCurrency");
      toCurrency = (Currency) context.get("endCurrency");
    } else {
      Map<String, Object> startCurrency = (Map<String, Object>) context.get("startCurrency");
      Map<String, Object> endCurrency = (Map<String, Object>) context.get("endCurrency");

      fromCurrency = currencyRepo.find(Long.parseLong(startCurrency.get("id").toString()));
      toCurrency = currencyRepo.find(Long.parseLong(endCurrency.get("id").toString()));
    }

    CurrencyConversionLine prevLine = null;

    if (fromCurrency != null && toCurrency != null) {

      if (context.get("id") != null)
        prevLine =
            cclRepo
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
            cclRepo
                .all()
                .filter(
                    "startCurrency.id = ?1 AND endCurrency.id = ?2",
                    fromCurrency.getId(),
                    toCurrency.getId())
                .order("-fromDate")
                .fetchOne();

      LOG.debug("Previous currency conversion line: {}", prevLine);
      fromCurrency = currencyRepo.find(fromCurrency.getId());
      toCurrency = currencyRepo.find(toCurrency.getId());
      BigDecimal rate = ccs.convert(fromCurrency, toCurrency);

      if (rate.compareTo(new BigDecimal(-1)) == 0)
        response.setFlash(I18n.get(IExceptionMessage.CURRENCY_6));
      else {
        response.setValue("variations", "0");
        if (context.get("_model").equals("com.axelor.apps.base.db.Wizard"))
          response.setValue("newExchangeRate", rate);
        else response.setValue("exchangeRate", rate);
        response.setValue("fromDate", appBaseService.getTodayDateTime());
        if (prevLine != null)
          response.setValue("variations", ccs.getVariations(rate, prevLine.getExchangeRate()));
      }
    }
  }
}
