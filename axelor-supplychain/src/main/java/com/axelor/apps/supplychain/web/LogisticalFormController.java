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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.supplychain.report.IReport;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class LogisticalFormController {

  public void print(ActionRequest request, ActionResponse response) {
    try {
      LogisticalForm logisticalForm = request.getContext().asType(LogisticalForm.class);

      String name =
          String.format("%s %s", I18n.get("Packing list"), logisticalForm.getDeliveryNumberSeq());

      String fileLink =
          ReportFactory.createReport(IReport.PACKING_LIST, name + " - ${date}")
              .addParam("LogisticalFormId", logisticalForm.getId())
              .addParam(
                  "Timezone",
                  logisticalForm.getCompany() != null
                      ? logisticalForm.getCompany().getTimezone()
                      : null)
              .addParam(
                  "Locale",
                  ReportSettings.getPrintingLocale(logisticalForm.getDeliverToCustomerPartner()))
              .generate()
              .getFileLink();

      response.setView(ActionView.define(name).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
