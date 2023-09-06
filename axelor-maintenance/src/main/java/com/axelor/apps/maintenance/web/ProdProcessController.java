/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.maintenance.web;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.maintenance.report.IReport;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class ProdProcessController {

  public void print(ActionRequest request, ActionResponse response) {
    try {
      ProdProcess prodProcess = request.getContext().asType(ProdProcess.class);
      String title = prodProcess.getName();
      String fileLink =
          ReportFactory.createReport(IReport.MAINTENANCE_PROD_PROCESS, title + "-${date}")
              .addParam("Locale", ReportSettings.getPrintingLocale(null))
              .addParam("ProdProcessId", prodProcess.getId().toString())
              .generate()
              .getFileLink();

      response.setView(ActionView.define(title).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
