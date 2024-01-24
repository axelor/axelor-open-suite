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
package com.axelor.apps.account.web;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.AccountingReportType;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.account.service.AccountingReportTypeService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.HandleExceptionResponse;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountingReportTypeController {
  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void defaultName(ActionRequest request, ActionResponse response) {
    AccountingReportType accountingReport = request.getContext().asType(AccountingReportType.class);

    try {
      Beans.get(AccountingReportTypeService.class).setDefaultName(accountingReport);
      response.setValue("name", accountingReport.getName());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @HandleExceptionResponse
  public void printReportAndRules(ActionRequest request, ActionResponse response)
      throws AxelorException {

    AccountingReportType accountingReportType =
        request.getContext().asType(AccountingReportType.class);

    String name = I18n.get("Accounting report and rules");

    String fileLink =
        ReportFactory.createReport(IReport.ACCOUNTING_REPORT_TYPE_AND_RULES, name + "-${date}")
            .addParam("AccountingReportTypeId", accountingReportType.getId())
            .addParam("Locale", ReportSettings.getPrintingLocale(null))
            .addParam("User", AuthUtils.getUser().getName())
            .generate()
            .getFileLink();

    logger.debug("Printing " + name);

    response.setView(ActionView.define(name).add("html", fileLink).map());
  }
}
