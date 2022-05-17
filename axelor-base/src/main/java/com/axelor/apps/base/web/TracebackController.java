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
package com.axelor.apps.base.web;

import com.axelor.app.AppSettings;
import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.PrintingSettings;
import com.axelor.apps.base.report.IReport;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.common.VersionUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.TraceBack;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;

public class TracebackController {

  public void printTraceback(ActionRequest request, ActionResponse response)
      throws AxelorException {
    TraceBack traceBack = request.getContext().asType(TraceBack.class);
    Long traceBackId = traceBack.getId();
    String name = "TraceBack " + traceBackId;
    Company activeCompany = AuthUtils.getUser().getActiveCompany();
    BigDecimal headerHeight = BigDecimal.ZERO;
    BigDecimal footerHeight = BigDecimal.ZERO;
    if (activeCompany != null) {
      PrintingSettings printingSettings = activeCompany.getPrintingSettings();
      headerHeight = printingSettings.getPdfHeaderHeight();
      footerHeight = printingSettings.getPdfFooterHeight();
    }
    String fileLink =
        ReportFactory.createReport(IReport.TRACEBACK, name + "-${date}")
            .addParam("Locale", ReportSettings.getPrintingLocale(null))
            .addParam("TracebackId", traceBackId)
            .addParam("SDKVersion", VersionUtils.getVersion().toString())
            .addParam("AOSVersion", AppSettings.get().get("application.version"))
            .addParam("HeaderHeight", headerHeight)
            .addParam("FooterHeight", footerHeight)
            .generate()
            .getFileLink();
    response.setView(ActionView.define(name).add("html", fileLink).map());
  }
}
