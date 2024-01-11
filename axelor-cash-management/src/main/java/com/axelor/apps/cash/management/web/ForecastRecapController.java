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
package com.axelor.apps.cash.management.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.cash.management.db.ForecastRecap;
import com.axelor.apps.cash.management.db.repo.ForecastRecapRepository;
import com.axelor.apps.cash.management.exception.CashManagementExceptionMessage;
import com.axelor.apps.cash.management.service.ForecastRecapService;
import com.axelor.apps.cash.management.translation.ITranslation;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForecastRecapController {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void populate(ActionRequest request, ActionResponse response) {
    try {
      ForecastRecap forecastRecap = request.getContext().asType(ForecastRecap.class);
      if (forecastRecap.getCompany() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(CashManagementExceptionMessage.FORECAST_COMPANY));
      }
      Beans.get(ForecastRecapService.class)
          .populate(Beans.get(ForecastRecapRepository.class).find(forecastRecap.getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fillStartingBalance(ActionRequest request, ActionResponse response) {
    ForecastRecap forecastRecap = request.getContext().asType(ForecastRecap.class);
    try {

      Company company = forecastRecap.getCompany();
      if (company != null && company.getCurrency() != null) {
        response.setValues(
            Beans.get(ForecastRecapService.class)
                .computeStartingBalanceForReporting(forecastRecap));
      }

    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void print(ActionRequest request, ActionResponse response) throws AxelorException {
    try {

      Context context = request.getContext();
      Long forecastRecapId = new Long(context.get("_forecastRecapId").toString());
      String reportType = (String) context.get("reportTypeSelect");
      ForecastRecap forecastRecap = Beans.get(ForecastRecapRepository.class).find(forecastRecapId);

      String fileLink =
          Beans.get(ForecastRecapService.class).getForecastRecapFileLink(forecastRecap, reportType);
      String title = I18n.get(ITranslation.CASH_MANAGEMENT_REPORT_TITLE);
      title += "-" + forecastRecap.getForecastRecapSeq();
      logger.debug("Printing {}", title);
      response.setView(ActionView.define(title).add("html", fileLink).map());
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
