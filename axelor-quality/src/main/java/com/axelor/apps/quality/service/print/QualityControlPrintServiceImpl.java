/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.quality.service.print;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.quality.db.QualityControl;
import com.axelor.apps.quality.exception.IExceptionMessage;
import com.axelor.apps.quality.report.IReport;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.tool.file.PdfTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.io.File;
import java.time.format.DateTimeFormatter;

public class QualityControlPrintServiceImpl {

  public String getFileName(QualityControl qualityControl) {

    return I18n.get("QualityControl") + " " + qualityControl.getReference();
  }

  public String printQualityControl(QualityControl qualityControl, String format)
      throws AxelorException {

    String fileName =
        I18n.get("QualityControl")
            + " - "
            + Beans.get(AppBaseService.class)
                .getTodayDate()
                .format(DateTimeFormatter.BASIC_ISO_DATE)
            + "."
            + format;

    return PdfTool.getFileLinkFromPdfFile(print(qualityControl, format), fileName);
  }

  public File print(QualityControl qualityControl, String format) throws AxelorException {

    ReportSettings reportSettings = prepareReportSettings(qualityControl, format);

    return reportSettings.generate().getFile();
  }

  public ReportSettings prepareReportSettings(QualityControl qualityControl, String format)
      throws AxelorException {

    if (qualityControl.getPrintingSettings() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          String.format(I18n.get(IExceptionMessage.QUALITY_CONTROL_MISSING_PRINTING_SETTINGS)),
          qualityControl);
    }

    String locale =
        ReportSettings.getPrintingLocale(qualityControl.getProject().getClientPartner());
    String title = getFileName(qualityControl);

    ReportSettings reportSetting =
        ReportFactory.createReport(IReport.QUALITY_CONTROL, title + " - ${date}");

    return reportSetting
        .addParam("QualityControlId", qualityControl.getId())
        .addParam("Locale", locale)
        .addFormat(format);
  }
}
