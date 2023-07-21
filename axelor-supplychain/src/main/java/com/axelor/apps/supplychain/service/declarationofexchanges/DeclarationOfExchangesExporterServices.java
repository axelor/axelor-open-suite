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
package com.axelor.apps.supplychain.service.declarationofexchanges;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.stock.db.Regime;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.db.DeclarationOfExchanges;
import com.axelor.apps.supplychain.report.IReport;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.file.CsvTool;
import com.google.common.io.MoreFiles;
import com.google.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DeclarationOfExchangesExporterServices extends DeclarationOfExchangesExporter {
  private static final String NAME_SERVICES = /*$$(*/ "European declaration of services" /*)*/;

  protected static final String LINE_NUM = /*$$(*/ "Line number" /*$$(*/;
  protected static final String FISC_VAL = /*$$(*/ "Fiscal value" /*$$(*/;
  protected static final String TAKER = /*$$(*/ "Taker" /*$$(*/;

  @Inject
  public DeclarationOfExchangesExporterServices(
      DeclarationOfExchanges declarationOfExchanges, ResourceBundle bundle) {
    super(
        declarationOfExchanges,
        bundle,
        NAME_SERVICES,
        new ArrayList<>(Arrays.asList(LINE_NUM, FISC_VAL, TAKER)));
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
      String[] data = new String[columnHeadersList.size()];

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

      data[columnHeadersList.indexOf(LINE_NUM)] = String.valueOf(lineNum++);
      data[columnHeadersList.indexOf(FISC_VAL)] = String.valueOf(fiscalValue);
      data[columnHeadersList.indexOf(TAKER)] = taxNbr;
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
        .addParam("UserId", Optional.ofNullable(AuthUtils.getUser()).map(User::getId).orElse(null))
        .addParam("Locale", ReportSettings.getPrintingLocale())
        .addParam(
            "Timezone",
            declarationOfExchanges.getCompany() != null
                ? declarationOfExchanges.getCompany().getTimezone()
                : null)
        .addFormat(declarationOfExchanges.getFormatSelect())
        .toAttach(declarationOfExchanges)
        .generate()
        .getFileLink();
  }
}
