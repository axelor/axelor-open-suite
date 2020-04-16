package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.report.IReport;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.tool.ModelTool;
import com.axelor.apps.tool.ThrowConsumer;
import com.axelor.apps.tool.file.PdfTool;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ManufOrderPrintServiceImpl implements ManufOrderPrintService {

  @Override
  public String printManufOrders(List<Long> ids) throws IOException {
    List<File> printedManufOrders = new ArrayList<>();
    ModelTool.apply(
        ManufOrder.class,
        ids,
        new ThrowConsumer<ManufOrder>() {

          @Override
          public void accept(ManufOrder manufOrder) throws Exception {
            printedManufOrders.add(print(manufOrder));
          }
        });
    String fileName = getManufOrdersFilename();
    return PdfTool.mergePdfToFileLink(printedManufOrders, fileName);
  }

  @Override
  public String printManufOrder(ManufOrder manufOrder) throws AxelorException {
    String fileName = getFileName(manufOrder);
    return PdfTool.getFileLinkFromPdfFile(print(manufOrder), fileName);
  }

  protected File print(ManufOrder manufOrder) throws AxelorException {
    ReportSettings reportSettings = prepareReportSettings(manufOrder);
    return reportSettings.generate().getFile();
  }

  public ReportSettings prepareReportSettings(ManufOrder manufOrder) throws AxelorException {
    String title = getFileName(manufOrder);
    ReportSettings reportSetting = ReportFactory.createReport(IReport.MANUF_ORDER, title);
    return reportSetting
        .addParam("Locale", ReportSettings.getPrintingLocale(null))
        .addParam("ManufOrderId", manufOrder.getId().toString())
        .addParam(
            "activateBarCodeGeneration",
            Beans.get(AppBaseService.class).getAppBase().getActivateBarCodeGeneration())
        .addFormat(ReportSettings.FORMAT_PDF);
  }

  @Override
  public String getManufOrdersFilename() {
    return I18n.get("Manufacturing orders")
        + " - "
        + Beans.get(AppBaseService.class).getTodayDate().format(DateTimeFormatter.BASIC_ISO_DATE)
        + "."
        + ReportSettings.FORMAT_PDF;
  }

  @Override
  public String getFileName(ManufOrder manufOrder) {
    return I18n.get("Manufacturing order")
        + " "
        + manufOrder.getManufOrderSeq()
        + "."
        + ReportSettings.FORMAT_PDF;
  }
}
