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
package com.axelor.apps.stock.service.stockmove.print;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.birt.template.BirtTemplateService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.ThrowConsumer;
import com.axelor.utils.helpers.ModelHelper;
import com.axelor.utils.helpers.file.PdfHelper;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConformityCertificatePrintServiceImpl implements ConformityCertificatePrintService {

  protected StockMoveService stockMoveService;
  protected StockConfigService stockConfigService;
  protected BirtTemplateService birtTemplateService;

  @Inject
  public ConformityCertificatePrintServiceImpl(
      StockMoveService stockMoveService,
      StockConfigService stockConfigService,
      BirtTemplateService birtTemplateService) {
    this.stockMoveService = stockMoveService;
    this.stockConfigService = stockConfigService;
    this.birtTemplateService = birtTemplateService;
  }

  @Override
  public String printConformityCertificates(List<Long> ids) throws IOException {
    List<File> printedConformityCertificates = new ArrayList<>();
    ModelHelper.apply(
        StockMove.class,
        ids,
        new ThrowConsumer<StockMove, Exception>() {
          @Override
          public void accept(StockMove stockMove) throws Exception {
            printedConformityCertificates.add(print(stockMove, ReportSettings.FORMAT_PDF));
          }
        });
    String fileName = getConformityCertificateFilesName(true, ReportSettings.FORMAT_PDF);
    return PdfHelper.mergePdfToFileLink(printedConformityCertificates, fileName);
  }

  @Override
  public File prepareReportSettings(StockMove stockMove, String format) throws AxelorException {
    stockMoveService.checkPrintingSettings(stockMove);

    BirtTemplate conformityCertificateBirtTemplate =
        stockConfigService
            .getStockConfig(stockMove.getCompany())
            .getConformityCertificateBirtTemplate();
    if (ObjectUtils.isEmpty(conformityCertificateBirtTemplate)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.BIRT_TEMPLATE_CONFIG_NOT_FOUND));
    }

    String title = getFileName(stockMove);
    return birtTemplateService.generateBirtTemplateFile(
        conformityCertificateBirtTemplate, stockMove, title + " - ${date}");
  }

  @Override
  public File print(StockMove stockMove, String format) throws AxelorException {
    return prepareReportSettings(stockMove, format);
  }

  @Override
  public String printConformityCertificate(StockMove stockMove, String format)
      throws AxelorException, IOException {
    String fileName = getConformityCertificateFilesName(false, ReportSettings.FORMAT_PDF);
    return PdfHelper.getFileLinkFromPdfFile(print(stockMove, format), fileName);
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
