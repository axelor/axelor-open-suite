/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.maintenance.web;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.maintenance.report.IReport;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.AxelorException;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class MaintenanceProdProcessController {

  public void print(ActionRequest request, ActionResponse response) throws AxelorException {

    ProdProcess prodProcess = request.getContext().asType(ProdProcess.class);
    String prodProcessId = prodProcess.getId().toString();
    String prodProcessLabel = prodProcess.getName().toString();

    String fileLink =
        ReportFactory.createReport(IReport.PROD_PROCESS, prodProcessLabel + "-${date}")
            .addParam("Locale", ReportSettings.getPrintingLocale(null))
            .addParam("ProdProcessId", prodProcessId)
            .generate()
            .getFileLink();

    response.setView(ActionView.define(prodProcessLabel).add("html", fileLink).map());
  }
}
