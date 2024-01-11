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
package com.axelor.apps.maintenance.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.maintenance.report.IReport;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.manuforder.ManufOrderPrintServiceImpl;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.inject.Beans;

public class ManufOrderPrintServiceMaintenanceImpl extends ManufOrderPrintServiceImpl {

  @Override
  public ReportSettings prepareReportSettings(ManufOrder manufOrder) {
    String title = getFileName(manufOrder);
    ReportSettings reportSetting =
        ReportFactory.createReport(
            manufOrder.getTypeSelect() == ManufOrderRepository.TYPE_MAINTENANCE
                ? IReport.MAINTENANCE_MANUF_ORDER
                : com.axelor.apps.production.report.IReport.MANUF_ORDER,
            title);
    return reportSetting
        .addParam("Locale", ReportSettings.getPrintingLocale(null))
        .addParam("ManufOrderId", manufOrder.getId().toString())
        .addParam(
            "activateBarCodeGeneration",
            Beans.get(AppBaseService.class).getAppBase().getActivateBarCodeGeneration())
        .addFormat(ReportSettings.FORMAT_PDF);
  }
}
