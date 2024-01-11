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
package com.axelor.apps.production.web;

import com.axelor.app.AppSettings;
import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.CostSheet;
import com.axelor.apps.production.report.IReport;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class CostSheetController {

  public void printCostSheetLineDetail(ActionRequest request, ActionResponse response) {

    try {
      CostSheet costSheet = request.getContext().asType(CostSheet.class);
      Long costSheetId = costSheet.getId();
      String name = I18n.get("Cost sheet");
      String fileLink =
          ReportFactory.createReport(IReport.COST_SHEET, name + "-${date}")
              .addParam("Locale", ReportSettings.getPrintingLocale(null))
              .addParam("Timezone", getTimezone(costSheet))
              .addParam("CostSheetId", costSheetId)
              .addParam(
                  "manageCostSheetGroup",
                  Beans.get(AppProductionService.class)
                      .getAppProduction()
                      .getManageCostSheetGroup())
              .addParam("BaseUrl", AppSettings.get().getBaseURL())
              .generate()
              .getFileLink();

      response.setCanClose(true);
      response.setView(ActionView.define(name).add("html", fileLink).map());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected String getTimezone(CostSheet costSheet) {
    if (costSheet.getManufOrder() == null || costSheet.getManufOrder().getCompany() == null) {
      return null;
    }
    return costSheet.getManufOrder().getCompany().getTimezone();
  }
}
