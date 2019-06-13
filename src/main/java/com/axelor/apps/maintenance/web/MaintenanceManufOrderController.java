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
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.maintenance.report.IReport;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaintenanceManufOrderController {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject ManufOrderRepository manufOrderRepo;

  public void print(ActionRequest request, ActionResponse response) {
    try {
      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);
      String manufOrderIds = "";

      @SuppressWarnings("unchecked")
      List<Integer> lstSelectedManufOrder = (List<Integer>) request.getContext().get("_ids");
      if (lstSelectedManufOrder != null) {
        for (Integer it : lstSelectedManufOrder) {
          manufOrderIds += it.toString() + ",";
        }
      }

      if (!manufOrderIds.equals("")) {
        manufOrderIds = manufOrderIds.substring(0, manufOrderIds.length() - 1);
        manufOrder = manufOrderRepo.find(new Long(lstSelectedManufOrder.get(0)));
      } else if (manufOrder.getId() != null) {
        manufOrderIds = manufOrder.getId().toString();
      }

      if (!manufOrderIds.equals("")) {

        String name;
        if (lstSelectedManufOrder == null) {
          name =
              String.format(
                  "%s %s",
                  I18n.get("Manufacturing order"),
                  Strings.nullToEmpty(manufOrder.getManufOrderSeq()));
        } else {
          name = I18n.get("Manufacturing orders");
        }

        String fileLink =
            ReportFactory.createReport(IReport.MANUF_ORDER, name + "-${date}")
                .addParam("Locale", ReportSettings.getPrintingLocale(null))
                .addParam("ManufOrderId", manufOrderIds)
                .addParam(
                    "activateBarCodeGeneration",
                    Beans.get(AppBaseService.class).getAppBase().getActivateBarCodeGeneration())
                .generate()
                .getFileLink();

        LOG.debug("Printing {}", name);

        response.setView(ActionView.define(name).add("html", fileLink).map());

      } else {
        response.setFlash(I18n.get(IExceptionMessage.MANUF_ORDER_1));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void printProdProcess(ActionRequest request, ActionResponse response) {

    try {
      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);
      String prodProcessId = manufOrder.getProdProcess().getId().toString();
      String prodProcessLable = manufOrder.getProdProcess().getName();

      String fileLink =
          ReportFactory.createReport(IReport.PROD_PROCESS, prodProcessLable + "-${date}")
              .addParam("Locale", ReportSettings.getPrintingLocale(null))
              .addParam("ProdProcessId", prodProcessId)
              .generate()
              .getFileLink();

      response.setView(ActionView.define(prodProcessLable).add("html", fileLink).map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
