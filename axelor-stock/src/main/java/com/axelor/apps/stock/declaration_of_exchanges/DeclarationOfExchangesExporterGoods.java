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
package com.axelor.apps.stock.declaration_of_exchanges;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.stock.db.CustomsCodeNomenclature;
import com.axelor.apps.stock.db.DeclarationOfExchanges;
import com.axelor.apps.stock.db.ModeOfTransport;
import com.axelor.apps.stock.db.NatureOfTransaction;
import com.axelor.apps.stock.db.Regime;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.CustomsCodeNomenclatureRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.report.IReport;
import com.axelor.apps.stock.service.StockMoveToolService;
import com.axelor.apps.tool.file.CsvTool;
import com.axelor.auth.AuthUtils;
import com.axelor.common.StringUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.io.MoreFiles;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DeclarationOfExchangesExporterGoods extends DeclarationOfExchangesExporter {
  protected static final String NAME_GOODS = /*$$(*/ "Declaration of exchanges of goods" /*)*/;

  protected enum Column implements DeclarationOfExchangesColumnHeader {
    LINE_NUM(/*$$(*/ "Line number" /*$$(*/),
    NOMENCLATURE(/*$$(*/ "Nomenclature" /*$$(*/),
    SRC_DST_COUNTRY(/*$$(*/ "Source or destination country" /*$$(*/),
    FISC_VAL(/*$$(*/ "Fiscal value" /*$$(*/),
    REGIME(/*$$(*/ "Regime" /*$$(*/),
    MASS(/*$$(*/ "Net mass" /*$$(*/),
    UNITS(/*$$(*/ "Supplementary unit" /*$$(*/),
    NAT_TRANS(/*$$(*/ "Nature of transaction" /*$$(*/),
    TRANSP(/*$$(*/ "Mode of transport" /*$$(*/),
    DEPT(/*$$(*/ "Department" /*$$(*/),
    COUNTRY_ORIG(/*$$(*/ "Country of origin" /*$$(*/),
    ACQUIRER(/*$$(*/ "Acquirer" /*$$(*/);

    private final String title;

    private Column(String title) {
      this.title = title;
    }

    @Override
    public String getTitle() {
      return title;
    }
  }

  protected CustomsCodeNomenclatureRepository customsCodeNomenclatureRepo;
  protected StockMoveToolService stockMoveToolService;

  public DeclarationOfExchangesExporterGoods(
      DeclarationOfExchanges declarationOfExchanges, ResourceBundle bundle) {
    super(declarationOfExchanges, bundle, NAME_GOODS, Column.values());
    this.customsCodeNomenclatureRepo = Beans.get(CustomsCodeNomenclatureRepository.class);
    this.stockMoveToolService = Beans.get(StockMoveToolService.class);
  }

  @Override
  public String exportToCSV() throws AxelorException {
    Path path = getFilePath();

    Period period = declarationOfExchanges.getPeriod();

    List<StockMoveLine> stockMoveLines =
        Beans.get(StockMoveLineRepository.class)
            .findForDeclarationOfExchanges(
                period.getFromDate(),
                period.getToDate(),
                declarationOfExchanges.getProductTypeSelect(),
                declarationOfExchanges.getStockMoveTypeSelect(),
                declarationOfExchanges.getCountry())
            .fetch();
    List<String[]> dataList = new ArrayList<>(stockMoveLines.size());
    int lineNum = 1;

    for (StockMoveLine stockMoveLine : stockMoveLines) {

      String[] data = exportLineToCsv(stockMoveLine, lineNum++);
      if (data != null) {
        dataList.add(data);
      }
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

  protected String[] exportLineToCsv(StockMoveLine stockMoveLine, int lineNum)
      throws AxelorException {

    String[] data = new String[Column.values().length];

    StockMove stockMove = stockMoveLine.getStockMove();

    String customsCode = stockMoveLine.getCustomsCode();

    if (StringUtils.isBlank(customsCode)) {
      if (stockMoveLine.getProduct() == null) {
        customsCode = I18n.get("Product is missing.");
      }

      if (stockMoveLine.getProduct().getCustomsCodeNomenclature() != null) {
        customsCode = stockMoveLine.getProduct().getCustomsCodeNomenclature().getCode();
      }

      if (StringUtils.isBlank(customsCode)) {
        customsCode =
            String.format(
                I18n.get("Customs code nomenclature is missing on product %s."),
                stockMoveLine.getProduct().getCode());
      }
    }

    BigDecimal fiscalValue =
        stockMoveLine
            .getCompanyUnitPriceUntaxed()
            .multiply(stockMoveLine.getRealQty())
            .setScale(0, RoundingMode.HALF_UP);

    // Only positive fiscal value should be take into account
    if (fiscalValue.compareTo(BigDecimal.ZERO) != 1) {
      return null;
    }

    Regime regime = stockMoveLine.getRegime();

    if (regime == null) {
      if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING) {
        regime = Regime.EXONERATED_SHIPMENT_AND_TRANSFER;
      } else if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING) {
        regime = Regime.INTRACOMMUNITY_ACQUISITION_TAXABLE_IN_FRANCE;
      }
    }

    BigDecimal totalNetMass = stockMoveLine.getTotalNetMass().setScale(0, RoundingMode.HALF_UP);

    CustomsCodeNomenclature customsCodeNomenclature =
        customsCodeNomenclatureRepo.findByCode(customsCode);

    String supplementaryUnit;

    if (customsCodeNomenclature != null
        && StringUtils.notBlank(customsCodeNomenclature.getSupplementaryUnit())) {
      supplementaryUnit = customsCodeNomenclature.getSupplementaryUnit();
    } else {
      supplementaryUnit = "";
    }

    NatureOfTransaction natTrans = stockMoveLine.getNatureOfTransaction();

    if (natTrans == null) {
      natTrans =
          stockMove.getIsReversion()
              ? NatureOfTransaction.RETURN_OF_GOODS
              : NatureOfTransaction.FIRM_PURCHASE_OR_SALE;
    }

    ModeOfTransport modeOfTransport = stockMove.getModeOfTransport();

    if (modeOfTransport == null) {
      modeOfTransport = ModeOfTransport.CONSIGNMENTS_BY_POST;
    }

    String srcDstCountry = "";
    String dept = "";
    try {
      Address partnerAddress = stockMoveToolService.getPartnerAddress(stockMoveLine.getStockMove());
      srcDstCountry = partnerAddress.getAddressL7Country().getAlpha2Code();
    } catch (AxelorException e) {
      srcDstCountry = e.getMessage();
    }
    try {
      Address companyAddress = stockMoveToolService.getCompanyAddress(stockMoveLine.getStockMove());
      dept = companyAddress.getCity().getDepartment().getCode();
    } catch (AxelorException e) {
      dept = e.getMessage();
    }

    String countryOrigCode;

    if (stockMoveLine.getCountryOfOrigin() != null) {
      countryOrigCode = stockMoveLine.getCountryOfOrigin().getAlpha2Code();
    } else {
      if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING) {
        countryOrigCode = srcDstCountry;
      } else {
        countryOrigCode = "";
      }
    }

    String taxNbr;
    if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING
        && stockMoveLine.getRegime() != Regime.OTHER_EXPEDITIONS) {

      if (stockMove.getPartner() == null) {
        taxNbr =
            String.format(I18n.get("Partner is missing on stock move %s."), stockMove.getName());
      } else if (StringUtils.isBlank(stockMove.getPartner().getTaxNbr())) {
        taxNbr =
            String.format(
                I18n.get("Tax number is missing on partner %s."), stockMove.getPartner().getName());
      } else {
        taxNbr = stockMove.getPartner().getTaxNbr();
      }
    } else {
      taxNbr = "";
    }

    data[Column.LINE_NUM.ordinal()] = String.valueOf(lineNum);
    data[Column.NOMENCLATURE.ordinal()] = customsCode;
    data[Column.SRC_DST_COUNTRY.ordinal()] = srcDstCountry;
    data[Column.FISC_VAL.ordinal()] = String.valueOf(fiscalValue);
    data[Column.REGIME.ordinal()] = String.valueOf(regime.getValue());
    data[Column.MASS.ordinal()] = String.valueOf(totalNetMass);
    data[Column.UNITS.ordinal()] = supplementaryUnit;
    data[Column.NAT_TRANS.ordinal()] = String.valueOf(natTrans.getValue());
    data[Column.TRANSP.ordinal()] = String.valueOf(modeOfTransport.getValue());
    data[Column.DEPT.ordinal()] = dept;
    data[Column.COUNTRY_ORIG.ordinal()] = countryOrigCode;
    data[Column.ACQUIRER.ordinal()] = taxNbr;

    return data;
  }

  @Override
  protected String exportToPDF() throws AxelorException {
    return ReportFactory.createReport(IReport.DECLARATION_OF_EXCHANGES_OF_GOODS, getTitle())
        .addParam("DeclarationOfExchangesId", declarationOfExchanges.getId())
        .addParam("UserId", AuthUtils.getUser().getId())
        .addParam("Locale", ReportSettings.getPrintingLocale())
        .addFormat(declarationOfExchanges.getFormatSelect())
        .toAttach(declarationOfExchanges)
        .generate()
        .getFileLink();
  }
}
