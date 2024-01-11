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

import com.axelor.app.AppSettings;
import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.PrintingSettings;
import com.axelor.apps.base.db.TraceBack;
import com.axelor.apps.base.report.IReport;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.common.Inflector;
import com.axelor.common.VersionUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TraceBackController {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Show reference view.
   *
   * @param request
   * @param response
   */
  public void showReference(ActionRequest request, ActionResponse response) {
    TraceBack traceBack = request.getContext().asType(TraceBack.class);

    if (Strings.isNullOrEmpty(traceBack.getRef())) {
      return;
    }

    Class<?> modelClass = JPA.model(traceBack.getRef());
    final Inflector inflector = Inflector.getInstance();
    String viewName = inflector.dasherize(modelClass.getSimpleName());

    LOG.debug("Showing anomaly reference ::: {}", viewName);

    ActionViewBuilder actionViewBuilder = ActionView.define(I18n.get("Reference"));
    actionViewBuilder.model(traceBack.getRef());

    if (traceBack.getRefId() != null) {
      actionViewBuilder.context("_showRecord", traceBack.getRefId());
    } else {
      actionViewBuilder.add("grid", String.format("%s-grid", viewName));
    }

    actionViewBuilder.add("form", String.format("%s-form", viewName));
    response.setView(actionViewBuilder.map());
  }

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
