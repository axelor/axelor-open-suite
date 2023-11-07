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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.birt.template.BirtTemplateService;
import com.axelor.apps.stock.db.CustomsCodeNomenclature;
import com.axelor.apps.stock.db.ModeOfTransport;
import com.axelor.apps.stock.db.NatureOfTransaction;
import com.axelor.apps.stock.db.Regime;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveToolService;
import com.axelor.apps.supplychain.db.DeclarationOfExchanges;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.helpers.file.CsvHelper;
import com.google.common.io.MoreFiles;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

public class DeclarationOfExchangesExporterGoods extends DeclarationOfExchangesExporter {
  protected static final String NAME_GOODS = /*$$(*/ "Declaration of exchanges of goods" /*)*/;

  protected static final String LINE_NUM = /*$$(*/ "Line number" /*$$(*/;
  protected static final String NOMENCLATURE = /*$$(*/ "Nomenclature" /*$$(*/;
  protected static final String SRC_DST_COUNTRY = /*$$(*/ "Source or destination country" /*$$(*/;
  protected static final String FISC_VAL = /*$$(*/ "Fiscal value" /*$$(*/;
  protected static final String REGIME = /*$$(*/ "Regime" /*$$(*/;
  protected static final String MASS = /*$$(*/ "Net mass" /*$$(*/;
  protected static final String UNITS = /*$$(*/ "Supplementary unit" /*$$(*/;
  protected static final String NAT_TRANS = /*$$(*/ "Nature of transaction" /*$$(*/;
  protected static final String TRANSP = /*$$(*/ "Mode of transport" /*$$(*/;
  protected static final String DEPT = /*$$(*/ "Department" /*$$(*/;
  protected static final String COUNTRY_ORIG = /*$$(*/ "Country of origin" /*$$(*/;
  protected static final String ACQUIRER = /*$$(*/ "Acquirer" /*$$(*/;
  protected static final String PRODUCT_CODE = /*$$(*/ "Product code" /*$$(*/;
  protected static final String PRODUCT_NAME = /*$$(*/ "Product name" /*$$(*/;
  protected static final String PARTNER_SEQ = /*$$(*/ "Partner" /*$$(*/;
  protected static final String INVOICE = /*$$(*/ "Invoice" /*$$(*/;

  protected StockMoveToolService stockMoveToolService;

  public DeclarationOfExchangesExporterGoods(
      DeclarationOfExchanges declarationOfExchanges, ResourceBundle bundle) {
    super(
        declarationOfExchanges,
        bundle,
        NAME_GOODS,
        new ArrayList<>(
            Arrays.asList(
                LINE_NUM,
                NOMENCLATURE,
                SRC_DST_COUNTRY,
                FISC_VAL,
                REGIME,
                MASS,
                UNITS,
                NAT_TRANS,
                TRANSP,
                DEPT,
                COUNTRY_ORIG,
                ACQUIRER,
                PRODUCT_CODE,
                PRODUCT_NAME,
                PARTNER_SEQ,
                INVOICE)));
    this.stockMoveToolService = Beans.get(StockMoveToolService.class);
    this.supplyChainConfigService = Beans.get(SupplyChainConfigService.class);
    this.birtTemplateService = Beans.get(BirtTemplateService.class);
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
                declarationOfExchanges.getCountry(),
                declarationOfExchanges.getCompany())
            .fetch();
    List<String[]> dataList = new ArrayList<>(stockMoveLines.size());
    int lineNum = 1;

    for (StockMoveLine stockMoveLine : stockMoveLines) {

      String[] data = exportLineToCsv(stockMoveLine, lineNum);
      if (data != null && data.length != 0) {
        dataList.add(data);
        lineNum++;
      }
    }

    try {
      MoreFiles.createParentDirectories(path);
      CsvHelper.csvWriter(
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

    String[] data = new String[columnHeadersList.size()];

    StockMove stockMove = stockMoveLine.getStockMove();

    String customsCode = stockMoveLine.getCustomsCode();

    Product product = stockMoveLine.getProduct();

    if (StringUtils.isBlank(customsCode)) {
      if (product == null) {
        customsCode = I18n.get("Product is missing.");
      } else {
        CustomsCodeNomenclature customsCodeNomenclature =
            (CustomsCodeNomenclature)
                Beans.get(ProductCompanyService.class)
                    .get(product, "customsCodeNomenclature", stockMove.getCompany());
        if (customsCodeNomenclature != null) {
          customsCode = customsCodeNomenclature.getCode();
        }
      }

      if (StringUtils.isBlank(customsCode)) {
        customsCode =
            String.format(
                I18n.get("Customs code nomenclature is missing on product %s."), product.getCode());
      }
    }

    BigDecimal fiscalValue =
        stockMoveLine
            .getCompanyUnitPriceUntaxed()
            .multiply(stockMoveLine.getRealQty())
            .setScale(0, RoundingMode.HALF_UP);

    // Only positive fiscal value should be take into account
    if (fiscalValue.compareTo(BigDecimal.ZERO) <= 0) {
      return new String[0];
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

    BigInteger supplementaryUnit =
        stockMoveLine.getRealQty().setScale(0, RoundingMode.CEILING).toBigInteger();

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

    String srcDstCountry;
    String dept;
    try {
      Address partnerAddress = stockMoveToolService.getPartnerAddress(stockMove, stockMoveLine);
      srcDstCountry = partnerAddress.getAddressL7Country().getAlpha2Code();
    } catch (AxelorException e) {
      srcDstCountry = e.getMessage();
    }
    try {
      Address companyAddress = stockMoveToolService.getCompanyAddress(stockMove, stockMoveLine);
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
      } else if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING
          && ObjectUtils.notEmpty(stockMoveLine.getFromStockLocation().getAddress())) {
        countryOrigCode =
            stockMoveLine.getFromStockLocation().getAddress().getAddressL7Country().getAlpha2Code();
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

    String partnerSeq = "";
    if (stockMove.getPartner() != null) {
      partnerSeq = stockMove.getPartner().getPartnerSeq();
    }

    String productCode = "";
    String productName = "";
    if (product != null) {
      productCode = product.getCode();
      productName = product.getName();
    }

    String invoiceId = "";
    StringBuilder invoiceIdBld = new StringBuilder();
    Set<Invoice> invoiceSet = stockMove.getInvoiceSet();
    if (invoiceSet != null) {
      for (Invoice invoice : invoiceSet) {
        if (invoice.getStatusSelect() == InvoiceRepository.STATUS_VENTILATED) {
          invoiceIdBld.append(invoice.getInvoiceId() + "|");
        }
      }

      invoiceId = invoiceIdBld.toString();
      if (invoiceId != null && !invoiceId.isEmpty()) {
        invoiceId = invoiceId.substring(0, invoiceId.length() - 1);
      }
    }

    data[columnHeadersList.indexOf(LINE_NUM)] = String.valueOf(lineNum);
    data[columnHeadersList.indexOf(NOMENCLATURE)] = customsCode;
    data[columnHeadersList.indexOf(SRC_DST_COUNTRY)] = srcDstCountry;
    data[columnHeadersList.indexOf(FISC_VAL)] = String.valueOf(fiscalValue);
    data[columnHeadersList.indexOf(REGIME)] = String.valueOf(regime.getValue());
    data[columnHeadersList.indexOf(MASS)] = String.valueOf(totalNetMass);
    data[columnHeadersList.indexOf(UNITS)] = String.valueOf(supplementaryUnit);
    data[columnHeadersList.indexOf(NAT_TRANS)] = String.valueOf(natTrans.getValue());
    data[columnHeadersList.indexOf(TRANSP)] = String.valueOf(modeOfTransport.getValue());
    data[columnHeadersList.indexOf(DEPT)] = dept;
    data[columnHeadersList.indexOf(COUNTRY_ORIG)] = countryOrigCode;
    data[columnHeadersList.indexOf(ACQUIRER)] = taxNbr;
    data[columnHeadersList.indexOf(PRODUCT_CODE)] = productCode;
    data[columnHeadersList.indexOf(PRODUCT_NAME)] = productName;
    data[columnHeadersList.indexOf(PARTNER_SEQ)] = partnerSeq;
    data[columnHeadersList.indexOf(INVOICE)] = invoiceId;

    return data;
  }

  @Override
  protected String exportToPDF() throws AxelorException {
    SupplyChainConfig supplyChainConfig =
        supplyChainConfigService.getSupplyChainConfig(declarationOfExchanges.getCompany());
    BirtTemplate declarationOfExchGoodsBirtTemplate =
        supplyChainConfig.getDeclarationOfExchGoodsBirtTemplate();
    if (ObjectUtils.isEmpty(declarationOfExchGoodsBirtTemplate)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.BIRT_TEMPLATE_CONFIG_NOT_FOUND));
    }
    return birtTemplateService.generateBirtTemplateLink(
        declarationOfExchGoodsBirtTemplate,
        declarationOfExchanges,
        null,
        getTitle(),
        true,
        declarationOfExchanges.getFormatSelect());
  }
}
