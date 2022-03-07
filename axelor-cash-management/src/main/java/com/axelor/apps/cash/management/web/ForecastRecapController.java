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
package com.axelor.apps.cash.management.web;

import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.cash.management.db.ForecastRecap;
import com.axelor.apps.cash.management.db.ForecastRecapLine;
import com.axelor.apps.cash.management.db.repo.ForecastRecapRepository;
import com.axelor.apps.cash.management.exception.IExceptionMessage;
import com.axelor.apps.cash.management.service.ForecastRecapService;
import com.axelor.apps.cash.management.translation.ITranslation;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForecastRecapController {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void populate(ActionRequest request, ActionResponse response) throws AxelorException {
    ForecastRecap forecastRecap = request.getContext().asType(ForecastRecap.class);
    if (forecastRecap.getCompany() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.FORECAST_COMPANY));
    }
    Beans.get(ForecastRecapService.class)
        .populate(Beans.get(ForecastRecapRepository.class).find(forecastRecap.getId()));
    response.setReload(true);
  }

  public void fillStartingBalance(ActionRequest request, ActionResponse response) {
    ForecastRecap forecastRecap = request.getContext().asType(ForecastRecap.class);
    try {
      if (forecastRecap.getBankDetails() != null) {
        BigDecimal amount =
            Beans.get(CurrencyService.class)
                .getAmountCurrencyConvertedAtDate(
                    forecastRecap.getBankDetails().getCurrency(),
                    forecastRecap.getCompany().getCurrency(),
                    forecastRecap.getBankDetails().getBalance(),
                    Beans.get(AppBaseService.class).getTodayDate(forecastRecap.getCompany()))
                .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
        forecastRecap.setStartingBalance(amount);
      } else {
        BigDecimal amount =
            Beans.get(CurrencyService.class)
                .getAmountCurrencyConvertedAtDate(
                    forecastRecap.getCompany().getDefaultBankDetails().getCurrency(),
                    forecastRecap.getCompany().getCurrency(),
                    forecastRecap.getCompany().getDefaultBankDetails().getBalance(),
                    Beans.get(AppBaseService.class).getTodayDate(forecastRecap.getCompany()))
                .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
        forecastRecap.setStartingBalance(amount);
      }
      response.setValues(forecastRecap);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void sales(ActionRequest request, ActionResponse response) throws AxelorException {
    Long id = new Long(request.getContext().get("_id").toString());
    ForecastRecapService forecastRecapService = Beans.get(ForecastRecapService.class);
    ForecastRecap forecastRecap = Beans.get(ForecastRecapRepository.class).find(id);
    forecastRecap.setForecastRecapLineList(new ArrayList<ForecastRecapLine>());
    Map<LocalDate, BigDecimal> mapExpected = new HashMap<LocalDate, BigDecimal>();
    Map<LocalDate, BigDecimal> mapConfirmed = new HashMap<LocalDate, BigDecimal>();
    if (forecastRecap.getOpportunitiesTypeSelect() != null
        && forecastRecap.getOpportunitiesTypeSelect()
            > ForecastRecapRepository.OPPORTUNITY_TYPE_NO) {
      forecastRecapService.getOpportunities(forecastRecap, mapExpected, mapConfirmed);
    }
    forecastRecapService.getInvoices(forecastRecap, mapExpected, mapConfirmed);
    forecastRecapService.getTimetablesOrOrders(forecastRecap, mapExpected, mapConfirmed);
    forecastRecapService.getForecasts(forecastRecap, mapExpected, mapConfirmed);
    List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    Set<LocalDate> keyList = mapExpected.keySet();
    for (LocalDate date : keyList) {
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("date", (Object) date);
      dataMap.put("amount", (Object) mapExpected.get(date));
      dataMap.put("type", (Object) I18n.get("Expected"));
      dataList.add(dataMap);
    }
    keyList = mapConfirmed.keySet();
    for (LocalDate date : keyList) {
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("date", (Object) date);
      dataMap.put("amount", (Object) mapConfirmed.get(date));
      dataMap.put("type", (Object) I18n.get("Confirmed"));
      dataList.add(dataMap);
    }
    response.setData(dataList);
  }

  public void spending(ActionRequest request, ActionResponse response) throws AxelorException {
    Long id = new Long(request.getContext().get("_id").toString());
    ForecastRecap forecastRecap = Beans.get(ForecastRecapRepository.class).find(id);
    List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    Map<LocalDate, BigDecimal> map = new HashMap<LocalDate, BigDecimal>();
    for (ForecastRecapLine forecastRecapLine : forecastRecap.getForecastRecapLineList()) {
      if (forecastRecapLine.getTypeSelect() == 2) {
        if (map.containsKey(forecastRecapLine.getEstimatedDate())) {
          map.put(
              forecastRecapLine.getEstimatedDate(),
              map.get(forecastRecapLine.getEstimatedDate()).add(forecastRecapLine.getAmount()));
        } else {
          map.put(forecastRecapLine.getEstimatedDate(), forecastRecapLine.getAmount());
        }
      }
    }
    Set<LocalDate> keyList = map.keySet();
    for (LocalDate date : keyList) {
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("date", (Object) date);
      dataMap.put("amount", (Object) map.get(date));
      dataList.add(dataMap);
    }
    response.setData(dataList);
  }

  public void marges(ActionRequest request, ActionResponse response) throws AxelorException {
    Long id = new Long(request.getContext().get("_id").toString());
    ForecastRecap forecastRecap = Beans.get(ForecastRecapRepository.class).find(id);
    List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
    Map<LocalDate, BigDecimal> map = new HashMap<LocalDate, BigDecimal>();
    for (ForecastRecapLine forecastRecapLine : forecastRecap.getForecastRecapLineList()) {
      if (forecastRecapLine.getTypeSelect() == 2) {
        if (map.containsKey(forecastRecapLine.getEstimatedDate())) {
          map.put(
              forecastRecapLine.getEstimatedDate(),
              map.get(forecastRecapLine.getEstimatedDate())
                  .subtract(forecastRecapLine.getAmount()));
        } else {
          map.put(
              forecastRecapLine.getEstimatedDate(),
              BigDecimal.ZERO.subtract(forecastRecapLine.getAmount()));
        }
      } else {
        if (map.containsKey(forecastRecapLine.getEstimatedDate())) {
          map.put(
              forecastRecapLine.getEstimatedDate(),
              map.get(forecastRecapLine.getEstimatedDate()).add(forecastRecapLine.getAmount()));
        } else {
          map.put(forecastRecapLine.getEstimatedDate(), forecastRecapLine.getAmount());
        }
      }
    }
    Set<LocalDate> keyList = map.keySet();
    for (LocalDate date : keyList) {
      Map<String, Object> dataMap = new HashMap<String, Object>();
      dataMap.put("date", (Object) date);
      dataMap.put("amount", (Object) map.get(date));
      dataList.add(dataMap);
    }
    response.setData(dataList);
  }

  public void print(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    Long forecastRecapId = new Long(context.get("_forecastRecapId").toString());
    String reportType = (String) context.get("reportTypeSelect");
    String fileLink =
        Beans.get(ForecastRecapService.class).getForecastRecapFileLink(forecastRecapId, reportType);
    String title = I18n.get(ITranslation.CASH_MANAGEMENT_REPORT_TITLE);
    title += forecastRecapId;
    logger.debug("Printing " + title);
    response.setView(ActionView.define(title).add("html", fileLink).map());
    response.setCanClose(true);
  }
}
