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
package com.axelor.apps.stock.service.stockmove.print;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.report.IReport;
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

public class ConformityCertificatePrintServiceImpl implements ConformityCertificatePrintService {

  @Override
  public String printConformityCertificates(List<Long> ids) throws IOException {
    List<File> printedConformityCertificates = new ArrayList<>();
    ModelTool.apply(
        StockMove.class,
        ids,
        new ThrowConsumer<StockMove, Exception>() {
          @Override
          public void accept(StockMove stockMove) throws Exception {
            printedConformityCertificates.add(print(stockMove, ReportSettings.FORMAT_PDF));
          }
        });
    String fileName = getConformityCertificateFilesName(true, ReportSettings.FORMAT_PDF);
    return PdfTool.mergePdfToFileLink(printedConformityCertificates, fileName);
  }

  @Override
  public ReportSettings prepareReportSettings(StockMove stockMove, String format)
      throws AxelorException {
    if (stockMove.getPrintingSettings() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          String.format(
              I18n.get(StockExceptionMessage.STOCK_MOVES_MISSING_PRINTING_SETTINGS),
              stockMove.getStockMoveSeq()),
          stockMove);
    }

    String locale = ReportSettings.getPrintingLocale(stockMove.getPartner());
    String title = getFileName(stockMove);

    ReportSettings reportSetting =
        ReportFactory.createReport(IReport.CONFORMITY_CERTIFICATE, title + " - ${date}");
    return reportSetting
        .addParam("StockMoveId", stockMove.getId())
        .addParam(
            "Timezone",
            stockMove.getCompany() != null ? stockMove.getCompany().getTimezone() : null)
        .addParam("Locale", locale)
        .addParam("HeaderHeight", stockMove.getPrintingSettings().getPdfHeaderHeight())
        .addParam("FooterHeight", stockMove.getPrintingSettings().getPdfFooterHeight())
        .addFormat(format);
  }

  @Override
  public File print(StockMove stockMove, String format) throws AxelorException {
    ReportSettings reportSettings = prepareReportSettings(stockMove, format);
    return reportSettings.generate().getFile();
  }

  @Override
  public String printConformityCertificate(StockMove stockMove, String format)
      throws AxelorException, IOException {
    String fileName = getConformityCertificateFilesName(false, ReportSettings.FORMAT_PDF);
    return PdfTool.getFileLinkFromPdfFile(print(stockMove, format), fileName);
  }

  /**
   * Return the name for the printed certificate of conformity.
   *
   * @param plural if there is one or multiple certificates.
   */
  public String getConformityCertificateFilesName(boolean plural, String format) {

    return I18n.get(plural ? "Conformity Certificates" : "Certificate of conformity")
        + " - "
        + Beans.get(AppBaseService.class)
            .getTodayDate(
                Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null))
            .format(DateTimeFormatter.BASIC_ISO_DATE)
        + "."
        + format;
  }

  @Override
  public String getFileName(StockMove stockMove) {

    return I18n.get("Certificate of conformity") + " " + stockMove.getStockMoveSeq();
  }
}
