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
package com.axelor.apps.supplychain.service.declarationofexchanges;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.stock.db.Regime;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.db.DeclarationOfExchanges;
import com.axelor.apps.supplychain.report.IReport;
import com.axelor.apps.tool.file.CsvTool;
import com.axelor.auth.AuthUtils;
import com.axelor.common.StringUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.io.MoreFiles;
import com.google.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DeclarationOfExchangesExporterServices extends DeclarationOfExchangesExporter {
  private static final String NAME_SERVICES = /*$$(*/ "European declaration of services" /*)*/;

  private enum Column implements DeclarationOfExchangesColumnHeader {
    LINE_NUM(/*$$(*/ "Line number" /*$$(*/),
    FISC_VAL(/*$$(*/ "Fiscal value" /*$$(*/),
    TAKER(/*$$(*/ "Taker" /*$$(*/);

    private final String title;

    private Column(String title) {
      this.title = title;
    }

    @Override
    public String getTitle() {
      return title;
    }
  }

  @Inject
  public DeclarationOfExchangesExporterServices(
      DeclarationOfExchanges declarationOfExchanges, ResourceBundle bundle) {
    super(declarationOfExchanges, bundle, NAME_SERVICES, Column.values());
  }

  // TODO: factorize code to parent.
  @Override
  protected String exportToCSV() throws AxelorException {
    Path path = getFilePath();

    Period period = declarationOfExchanges.getPeriod();

    List<StockMoveLine> stockMoveLines =
        Beans.get(StockMoveLineRepository.class)
            .findForDeclarationOfExchanges(
                period.getFromDate(),
                period.getToDate(),
                declarationOfExchanges.getProductTypeSelect(),
                declarationOfExchanges.getStockMoveTypeSelect(),
                declarationOfExchanges.getCountry(),
                declarationOfExchanges.getCompany())
            .fetch();
    List<String[]> dataList = new ArrayList<>(stockMoveLines.size());
    int lineNum = 1;

    for (StockMoveLine stockMoveLine : stockMoveLines) {
      String[] data = new String[Column.values().length];

      StockMove stockMove = stockMoveLine.getStockMove();

      BigDecimal fiscalValue =
          stockMoveLine
              .getUnitPriceUntaxed()
              .multiply(stockMoveLine.getRealQty())
              .setScale(0, RoundingMode.HALF_UP);

      String taxNbr;

      if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING
          && stockMoveLine.getRegime() != Regime.OTHER_EXPEDITIONS) {

        if (stockMove.getPartner() == null) {
          taxNbr =
              String.format(I18n.get("Partner is missing on stock move %s."), stockMove.getName());
        }

        if (StringUtils.isBlank(stockMove.getPartner().getTaxNbr())) {
          taxNbr =
              String.format(
                  I18n.get("Tax number is missing on partner %s."),
                  stockMove.getPartner().getName());
        }

        taxNbr = stockMove.getPartner().getTaxNbr();
      } else {
        taxNbr = "";
      }

      data[Column.LINE_NUM.ordinal()] = String.valueOf(lineNum++);
      data[Column.FISC_VAL.ordinal()] = String.valueOf(fiscalValue);
      data[Column.TAKER.ordinal()] = taxNbr;
      dataList.add(data);
    }

    try {
      MoreFiles.createParentDirectories(path);
      CsvTool.csvWriter(
          path.getParent().toString(),
          path.getFileName().toString(),
          ';',
          getTranslatedHeaders(),
          dataList);
    } catch (IOException e) {
      throw new AxelorException(
          e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getLocalizedMessage());
    }

    return attach(path.toString());
  }

  @Override
  protected String exportToPDF() throws AxelorException {
    return ReportFactory.createReport(IReport.DECLARATION_OF_SERVICES, getTitle())
        .addParam("DeclarationOfExchangesId", declarationOfExchanges.getId())
        .addParam("UserId", AuthUtils.getUser().getId())
        .addParam("Locale", ReportSettings.getPrintingLocale())
        .addFormat(declarationOfExchanges.getFormatSelect())
        .toAttach(declarationOfExchanges)
        .generate()
        .getFileLink();
  }
}
