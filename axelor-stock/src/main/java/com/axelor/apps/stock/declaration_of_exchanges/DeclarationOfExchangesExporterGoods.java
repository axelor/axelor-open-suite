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
  private static final String NAME_GOODS = /*$$(*/ "Declaration of exchanges of goods" /*)*/;

  private enum Column implements DeclarationOfExchangesColumnHeader {
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

  public DeclarationOfExchangesExporterGoods(
      DeclarationOfExchanges declarationOfExchanges, ResourceBundle bundle) {
    super(declarationOfExchanges, bundle, NAME_GOODS, Column.values());
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
    int lineNum = 0;

    StockMoveToolService stockMoveToolService = Beans.get(StockMoveToolService.class);
    CustomsCodeNomenclatureRepository customsCodeNomenclatureRepo =
        Beans.get(CustomsCodeNomenclatureRepository.class);

    for (StockMoveLine stockMoveLine : stockMoveLines) {
      String[] data = new String[Column.values().length];

      StockMove stockMove = stockMoveLine.getStockMove();

      if (stockMove == null) {
        throw new AxelorException(
            stockMoveLine, TraceBackRepository.CATEGORY_NO_VALUE, I18n.get("Missing stock move"));
      }

      String customsCode = stockMoveLine.getCustomsCode();

      if (StringUtils.isBlank(customsCode)) {
        if (stockMoveLine.getProduct() == null) {
          throw new AxelorException(
              stockMoveLine,
              TraceBackRepository.CATEGORY_NO_VALUE,
              I18n.get("Product is missing."));
        }

        if (stockMoveLine.getProduct().getCustomsCodeNomenclature() != null) {
          customsCode = stockMoveLine.getProduct().getCustomsCodeNomenclature().getCode();
        }

        if (StringUtils.isBlank(customsCode)) {
          throw new AxelorException(
              stockMoveLine.getProduct(),
              TraceBackRepository.CATEGORY_NO_VALUE,
              I18n.get("Customs code nomenclature is missing on product %s."),
              stockMoveLine.getProduct().getFullName());
        }
      }

      BigDecimal fiscalValue =
          stockMoveLine
              .getUnitPriceUntaxed()
              .multiply(stockMoveLine.getRealQty())
              .setScale(0, RoundingMode.HALF_UP);

      Regime regime = stockMoveLine.getRegime();

      if (regime == null) {
        if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING) {
          regime = Regime.EXONERATED_SHIPMENT_AND_TRANSFER;
        } else if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING) {
          regime = Regime.INTRACOMMUNITY_ACQUISITION_TAXABLE_IN_FRANCE;
        } else {
          throw new AxelorException(
              stockMove,
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get("Unexpected stock move type"));
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

      Address partnerAddress = stockMoveToolService.getPartnerAddress(stockMoveLine.getStockMove());
      Address companyAddress = stockMoveToolService.getCompanyAddress(stockMoveLine.getStockMove());
      String countryOrigCode;

      if (stockMoveLine.getCountryOfOrigin() != null) {
        countryOrigCode = stockMoveLine.getCountryOfOrigin().getAlpha2Code();
      } else {
        if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING) {
          countryOrigCode = partnerAddress.getAddressL7Country().getAlpha2Code();
        } else {
          countryOrigCode = "";
        }
      }

      String taxNbr;

      if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING
          && stockMoveLine.getRegime() != Regime.OTHER_EXPEDITIONS) {

        if (stockMove.getPartner() == null) {
          throw new AxelorException(
              stockMove,
              TraceBackRepository.CATEGORY_NO_VALUE,
              I18n.get("Partner is missing on stock move %s."),
              stockMove.getName());
        }

        if (StringUtils.isBlank(stockMove.getPartner().getTaxNbr())) {
          throw new AxelorException(
              stockMove.getPartner(),
              TraceBackRepository.CATEGORY_NO_VALUE,
              I18n.get("Tax number is missing on partner %s."),
              stockMove.getPartner().getName());
        }

        taxNbr = stockMove.getPartner().getTaxNbr();
      } else {
        taxNbr = "";
      }

      data[Column.LINE_NUM.ordinal()] = String.valueOf(lineNum++ % 10 + 1);
      data[Column.NOMENCLATURE.ordinal()] = customsCode;
      data[Column.SRC_DST_COUNTRY.ordinal()] = partnerAddress.getAddressL7Country().getAlpha2Code();
      data[Column.FISC_VAL.ordinal()] = String.valueOf(fiscalValue);
      data[Column.REGIME.ordinal()] = String.valueOf(regime.getValue());
      data[Column.MASS.ordinal()] = String.valueOf(totalNetMass);
      data[Column.UNITS.ordinal()] = supplementaryUnit;
      data[Column.NAT_TRANS.ordinal()] = String.valueOf(natTrans.getValue());
      data[Column.TRANSP.ordinal()] = String.valueOf(modeOfTransport.getValue());
      data[Column.DEPT.ordinal()] = companyAddress.getCity().getDepartment().getCode();
      data[Column.COUNTRY_ORIG.ordinal()] = countryOrigCode;
      data[Column.ACQUIRER.ordinal()] = taxNbr;
      dataList.add(data);
    }

    try {
      MoreFiles.createParentDirectories(path);
      CsvTool.csvWriter(
          path.getParent().toString(),
          path.getFileName().toString(),
          ',',
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
