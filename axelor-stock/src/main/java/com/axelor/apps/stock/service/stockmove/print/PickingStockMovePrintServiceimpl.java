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
import com.axelor.apps.base.service.exception.TraceBackService;
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

public class PickingStockMovePrintServiceimpl implements PickingStockMovePrintService {

  protected StockMoveService stockMoveService;
  protected StockConfigService stockConfigService;
  protected BirtTemplateService birtTemplateService;

  @Inject
  public PickingStockMovePrintServiceimpl(
      StockMoveService stockMoveService,
      StockConfigService stockConfigService,
      BirtTemplateService birtTemplateService) {
    this.stockMoveService = stockMoveService;
    this.stockConfigService = stockConfigService;
    this.birtTemplateService = birtTemplateService;
  }

  @Override
  public String printStockMoves(List<Long> ids, String userType)
      throws IOException, AxelorException {
    List<File> printedStockMoves = new ArrayList<>();
    int errorCount =
        ModelHelper.apply(
            StockMove.class,
            ids,
            new ThrowConsumer<StockMove, Exception>() {
              @Override
              public void accept(StockMove stockMove) throws Exception {
                try {
                  printedStockMoves.add(print(stockMove, ReportSettings.FORMAT_PDF));
                } catch (Exception e) {
                  TraceBackService.trace(e);
                  throw e;
                }
              }
            });
    if (errorCount > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FILE_COULD_NOT_BE_GENERATED));
    }
    stockMoveService.setPickingStockMovesEditDate(ids, userType);
    String fileName = getStockMoveFilesName(true, ReportSettings.FORMAT_PDF);
    return PdfHelper.mergePdfToFileLink(printedStockMoves, fileName);
  }

  @Override
  public File prepareReportSettings(StockMove stockMove, String format) throws AxelorException {
    stockMoveService.checkPrintingSettings(stockMove);
    BirtTemplate pickingStockMoveBirtTemplate =
        stockConfigService.getStockConfig(stockMove.getCompany()).getPickingStockMoveBirtTemplate();
    if (ObjectUtils.isEmpty(pickingStockMoveBirtTemplate)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.BIRT_TEMPLATE_CONFIG_NOT_FOUND));
    }

    String title = getFileName(stockMove);

    return birtTemplateService.generateBirtTemplateFile(
        pickingStockMoveBirtTemplate, stockMove, title + " - ${date}");
  }

  @Override
  public File print(StockMove stockMove, String format) throws AxelorException {
    return prepareReportSettings(stockMove, format);
  }

  @Override
  public String printStockMove(StockMove stockMove, String format, String userType)
      throws AxelorException, IOException {
    stockMoveService.setPickingStockMoveEditDate(stockMove, userType);
    String fileName = getStockMoveFilesName(false, ReportSettings.FORMAT_PDF);
    return PdfHelper.getFileLinkFromPdfFile(print(stockMove, format), fileName);
  }

  /**
   * Return the name for the printed stock move.
   *
   * @param plural if there is one or multiple stock moves.
   */
  public String getStockMoveFilesName(boolean plural, String format) {

    return I18n.get(plural ? "Stock moves" : "Stock move")
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

    return I18n.get("Stock Move") + " " + stockMove.getStockMoveSeq();
  }
}
