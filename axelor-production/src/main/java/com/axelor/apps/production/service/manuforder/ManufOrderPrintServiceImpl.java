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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.report.IReport;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.ModelTool;
import com.axelor.utils.ThrowConsumer;
import com.axelor.utils.file.PdfTool;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ManufOrderPrintServiceImpl implements ManufOrderPrintService {

  @Override
  public String printManufOrders(List<Long> ids) throws IOException {
    List<File> printedManufOrders = new ArrayList<>();
    ModelTool.apply(
        ManufOrder.class,
        ids,
        new ThrowConsumer<ManufOrder, Exception>() {

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

  @Override
  public ReportSettings prepareReportSettings(ManufOrder manufOrder) {
    String title = getFileName(manufOrder);
    ReportSettings reportSetting = ReportFactory.createReport(IReport.MANUF_ORDER, title);
    return reportSetting
        .addParam("Locale", ReportSettings.getPrintingLocale(null))
        .addParam(
            "Timezone",
            manufOrder.getCompany() != null ? manufOrder.getCompany().getTimezone() : null)
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
        + Beans.get(AppBaseService.class)
            .getTodayDate(
                Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null))
            .format(DateTimeFormatter.BASIC_ISO_DATE)
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
