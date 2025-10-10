/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.web;

import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.account.service.analytic.TradingNameAnalyticService;
import com.axelor.apps.account.service.analytic.TradingNameAnalyticServiceImpl;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class TradingNameController {

  public void showAnalyticPanel(ActionRequest request, ActionResponse response) {
    try {
      TradingName tradingName = request.getContext().asType(TradingName.class);
      boolean emptyTemplate =
          Beans.get(TradingNameAnalyticService.class).isAnalyticTypeByTradingName(tradingName);
      response.setAttr("analyticDistributionPanel", "hidden", emptyTemplate);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void emptyAnalyticDistributionTemplate(ActionRequest request, ActionResponse response) {
    try {
      TradingName tradingName = request.getContext().asType(TradingName.class);
      if (tradingName.getAnalyticDistributionTemplate() == null) {
        return;
      }
      boolean emptyTemplate =
          Beans.get(TradingNameAnalyticService.class).isAnalyticTypeByTradingName(tradingName);

      if (emptyTemplate) {
        response.setValue("analyticDistributionTemplate", null);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setAnalyticDistributionTemplate(ActionRequest request, ActionResponse response) {
    try {
      TradingName tradingName = request.getContext().asType(TradingName.class);

      response.setAttr(
          "analyticDistributionTemplate",
          "domain",
          Beans.get(TradingNameAnalyticServiceImpl.class).getDomainOnCompany(tradingName));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @ErrorException
  public void setDomainAnalyticDistributionTemplate(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    TradingName tradingName = context.asType(TradingName.class);

    response.setAttr(
        "analyticDistributionTemplate",
        "domain",
        Beans.get(AnalyticAttrsService.class)
            .getAnalyticDistributionTemplateDomain(
                tradingName.getPartner(),
                null,
                tradingName.getCompany(),
                tradingName,
                null,
                false));
  }
}
