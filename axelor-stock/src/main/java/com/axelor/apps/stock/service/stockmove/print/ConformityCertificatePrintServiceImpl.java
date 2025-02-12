/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.printing.template.PrintingTemplateHelper;
import com.axelor.apps.base.service.printing.template.PrintingTemplatePrintService;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
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
  protected PrintingTemplatePrintService printingTemplatePrintService;

  @Inject
  public ConformityCertificatePrintServiceImpl(
      StockMoveService stockMoveService,
      StockConfigService stockConfigService,
      PrintingTemplatePrintService printingTemplatePrintService) {
    this.stockMoveService = stockMoveService;
    this.stockConfigService = stockConfigService;
    this.printingTemplatePrintService = printingTemplatePrintService;
  }

  @Override
  public String printConformityCertificates(List<Long> ids) throws IOException, AxelorException {
    List<File> printedConformityCertificates = new ArrayList<>();
    ModelHelper.apply(
        StockMove.class,
        ids,
        new ThrowConsumer<StockMove, Exception>() {
          @Override
          public void accept(StockMove stockMove) throws Exception {
            printedConformityCertificates.add(print(stockMove));
          }
        });
    String fileName = getConformityCertificateFilesName(true);
    return PrintingTemplateHelper.mergeToFileLink(printedConformityCertificates, fileName);
  }

  @Override
  public File print(StockMove stockMove) throws AxelorException {
    stockMoveService.checkPrintingSettings(stockMove);

    PrintingTemplate conformityCertificatePrintTemplate =
        stockConfigService
            .getStockConfig(stockMove.getCompany())
            .getConformityCertificatePrintTemplate();
    if (ObjectUtils.isEmpty(conformityCertificatePrintTemplate)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.TEMPLATE_CONFIG_NOT_FOUND));
    }

    return printingTemplatePrintService.getPrintFile(
        conformityCertificatePrintTemplate, new PrintingGenFactoryContext(stockMove));
  }

  @Override
  public String printConformityCertificate(StockMove stockMove)
      throws AxelorException, IOException {
    return PrintingTemplateHelper.getFileLink(print(stockMove));
  }

  /**
   * Return the name for the printed certificate of conformity.
   *
   * @param plural if there is one or multiple certificates.
   */
  public String getConformityCertificateFilesName(boolean plural) {

    return I18n.get(plural ? "Conformity Certificates" : "Certificate of conformity")
        + " - "
        + Beans.get(AppBaseService.class)
            .getTodayDate(
                Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null))
            .format(DateTimeFormatter.BASIC_ISO_DATE);
  }

  @Override
  public String getFileName(StockMove stockMove) {

    return I18n.get("Certificate of conformity") + " " + stockMove.getStockMoveSeq();
  }
}
