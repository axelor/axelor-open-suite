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
