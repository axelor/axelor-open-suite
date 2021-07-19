/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.AccountingReportMoveLine;
import com.axelor.apps.account.db.TaxPaymentMoveLine;
import com.axelor.apps.account.db.repo.AccountingReportMoveLineRepository;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.db.repo.TaxPaymentMoveLineRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.apps.tool.file.FileTool;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class AccountingReportMoveLineServiceImpl implements AccountingReportMoveLineService {

  protected static final String DATE_FORMAT_DDMMYYYY = "ddMMyyyy";

  protected AccountingReportMoveLineRepository accountingReportMoveLineRepo;

  protected AccountingReportRepository accountingReportRepo;

  protected TaxPaymentMoveLineRepository taxPaymentmoveLineRepo;

  protected AppAccountService appAccountService;

  protected AccountConfigService accountConfigService;

  @Inject
  public AccountingReportMoveLineServiceImpl(
      AccountingReportMoveLineRepository accountingReportMoveLineRepo,
      AccountingReportRepository accountingReportRepo,
      TaxPaymentMoveLineRepository taxPaymentmoveLineRepo,
      AppAccountService appAccountService,
      AccountConfigService accountConfigService) {
    this.accountingReportMoveLineRepo = accountingReportMoveLineRepo;
    this.taxPaymentmoveLineRepo = taxPaymentmoveLineRepo;
    this.accountingReportRepo = accountingReportRepo;
    this.appAccountService = appAccountService;
    this.accountConfigService = accountConfigService;
  }

  @Override
  public void createAccountingReportMoveLines(
      List<BigInteger> taxPaymentMoveLineIds, AccountingReport accountingReport) {

    int i = 0;
    for (BigInteger id : taxPaymentMoveLineIds) {
      TaxPaymentMoveLine taxPaymentMoveLine = taxPaymentmoveLineRepo.find(id.longValue());
      if (taxPaymentMoveLine != null) {
        createAccountingReportMoveLine(
            taxPaymentMoveLine, accountingReportRepo.find(accountingReport.getId()));
        i++;
        if (i % 10 == 0) {
          JPA.clear();
        }
      }
    }
  }

  @Transactional
  @Override
  public void createAccountingReportMoveLine(
      TaxPaymentMoveLine taxPaymentMoveLine, AccountingReport accountingReport) {

    AccountingReportMoveLine accountingReportMoveLine =
        new AccountingReportMoveLine(taxPaymentMoveLine, accountingReport);
    accountingReportMoveLine.setExcludeFromDas2Report(
        taxPaymentMoveLine.getReconcile().getCreditMoveLine().getExcludeFromDas2Report());
    accountingReportMoveLineRepo.save(accountingReportMoveLine);
  }

  @Override
  @Transactional
  public void processExportMoveLine(
      AccountingReportMoveLine reportMoveLine, AccountingReport accountingExport) {

    reportMoveLine.setAccountingExport(accountingExport);
    accountingReportMoveLineRepo.save(reportMoveLine);
  }

  @Override
  public List<Partner> getDasToDeclarePartnersFromAccountingExport(
      AccountingReport accountingExport) throws AxelorException {

    List<AccountingReportMoveLine> reportMoveLines =
        getDasToDeclareLinesFromAccountingExport(accountingExport);

    List<Partner> partners = Lists.newArrayList();

    if (CollectionUtils.isEmpty(reportMoveLines)) {
      return partners;
    }

    for (AccountingReportMoveLine reportMoveLine : reportMoveLines) {

      Partner partner =
          reportMoveLine
              .getTaxPaymentMoveLine()
              .getReconcile()
              .getCreditMoveLine()
              .getMove()
              .getPartner();
      if (partner == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.ACCOUNTING_REPORT_DAS2_MOVE_LINE_PARTNER_MISSING),
            reportMoveLine
                .getTaxPaymentMoveLine()
                .getReconcile()
                .getCreditMoveLine()
                .getMove()
                .getReference());
      }
      if (!partners.contains(partner)) {
        partners.add(partner);
      }
    }
    return partners;
  }

  @Override
  public List<AccountingReportMoveLine> getDasToDeclareLinesFromAccountingExport(
      AccountingReport accountingExport) {

    return accountingReportMoveLineRepo
        .all()
        .filter(
            "self.accountingExport = ?1 AND self.excludeFromDas2Report != true AND self.exported != true",
            accountingExport)
        .fetch();
  }

  @Override
  public MetaFile generateN4DSFile(AccountingReport accountingExport, String fileName)
      throws AxelorException, IOException {

    List<String> lines = Lists.newArrayList();
    lines.addAll(generateN4DSLines(accountingExport));

    File file =
        FileTool.writer(
            Beans.get(AppService.class).getDataExportDir(), fileName, (List<String>) lines);
    InputStream is = new FileInputStream(file);
    return Beans.get(MetaFiles.class).upload(is, fileName);
  }

  @Override
  public List<String> generateN4DSLines(AccountingReport accountingExport) throws AxelorException {

    List<String> lines = Lists.newArrayList();

    Partner companyPartner = accountingExport.getCompany().getPartner();
    Address address = companyPartner.getMainAddress();
    String alpha2code = address.getAddressL7Country().getAlpha2Code();
    String registrationCode = companyPartner.getRegistrationCode().replaceAll(" " , "");
    String siren = computeSiren(registrationCode, alpha2code);
    String nic = computeNic(registrationCode, alpha2code);

    // TODO regex identités et adresses

    // S10.G10.00
    lines.add(setN4DSLine("S10.G01.00.001.001", siren));
    lines.add(setN4DSLine("S10.G01.00.001.002", nic));
    lines.add(setN4DSLine("S10.G01.00.002", companyPartner.getName()));
    String addressL2L3 = null;
    if (!Strings.isNullOrEmpty(address.getAddressL2())) {
    	addressL2L3 = address.getAddressL2().trim();
    }
    if(!Strings.isNullOrEmpty(address.getAddressL3())) {
    	if (Strings.isNullOrEmpty(addressL2L3))  {
    		addressL2L3 = address.getAddressL3().trim();
    	}else {
    		addressL2L3 += " " +address.getAddressL3().trim();
    	}
    }
    if (!Strings.isNullOrEmpty(addressL2L3)) {
      lines.add(setN4DSLine("S10.G01.00.003.001", addressL2L3));
    }
    if (!Strings.isNullOrEmpty(address.getAddressL4())) {
      lines.add(setN4DSLine("S10.G01.00.003.006", address.getAddressL4()));
    }
    if (alpha2code.equals("FR")) {
      lines.add(setN4DSLine("S10.G01.00.003.010", address.getCity().getZip()));
      lines.add(setN4DSLine("S10.G01.00.003.012", address.getCity().getName()));
    } else {
      lines.add(setN4DSLine("S10.G01.00.003.013", alpha2code));
      lines.add(setN4DSLine("S10.G01.00.003.016", address.getAddressL7Country().getCog()));
    }
    lines.add(setN4DSLine("S10.G01.00.004", "0"));
    lines.add(setN4DSLine("S10.G01.00.005", "AXELOR OPEN SUITE"));
    lines.add(setN4DSLine("S10.G01.00.006", "AXELOR"));
    lines.add(setN4DSLine("S10.G01.00.009", "40"));
    lines.add(setN4DSLine("S10.G01.00.010", "02"));
    lines.add(setN4DSLine("S10.G01.00.011", appAccountService.getAppAccount().getDasActiveNorm()));
    lines.add(setN4DSLine("S10.G01.00.012", "01"));

    // S10.G01.01
    Partner dasContactPartner =
        accountConfigService.getAccountConfig(accountingExport.getCompany()).getDasContactPartner();
    lines.add(
        setN4DSLine(
            "S10.G01.01.001.001",
            dasContactPartner.getTitleSelect().equals(PartnerRepository.PARTNER_TITLE_MS)
                ? "02"
                : "01"));
    lines.add(setN4DSLine("S10.G01.01.001.002", dasContactPartner.getSimpleFullName()));
    lines.add(setN4DSLine("S10.G01.01.002", "01"));
    lines.add(setN4DSLine("S10.G01.01.005", dasContactPartner.getEmailAddress().getAddress()));
    lines.add(
        setN4DSLine(
            "S10.G01.01.006",
            ObjectUtils.firstNonNull(
                dasContactPartner.getFixedPhone(), dasContactPartner.getMobilePhone())));
    if (!Strings.isNullOrEmpty(dasContactPartner.getFax())) {
      lines.add(setN4DSLine("S10.G01.01.007", dasContactPartner.getFax()));
    }

    // S10.G01.05
    lines.add(setN4DSLine("S10.G01.05.013.001", siren));
    lines.add(setN4DSLine("S10.G01.05.013.002", nic));
    lines.add(setN4DSLine("S10.G01.05.015.001", dasContactPartner.getEmailAddress().getAddress()));

    // S20.G01.00
    lines.add(setN4DSLine("S20.G01.00.001", siren));
    lines.add(setN4DSLine("S20.G01.00.002", companyPartner.getName()));
    lines.add(
        setN4DSLine(
            "S20.G01.00.003.001",
            accountingExport
                .getDateFrom()
                .format(DateTimeFormatter.ofPattern(DATE_FORMAT_DDMMYYYY))));
    lines.add(
        setN4DSLine(
            "S20.G01.00.003.002",
            accountingExport
                .getDateTo()
                .format(DateTimeFormatter.ofPattern(DATE_FORMAT_DDMMYYYY))));
    lines.add(setN4DSLine("S20.G01.00.004.001", "12"));
    lines.add(
        setN4DSLine("S20.G01.00.004.001", accountingExport.getComplementaryExport() ? "52" : "51"));
    // TODO Numéro de fraction de déclaration
    //	  lines.add(setN4DSLine("S20.G01.00.005",null));
    lines.add(setN4DSLine("S20.G01.00.007", "01"));
    lines.add(setN4DSLine("S20.G01.00.008", nic));
    if (!Strings.isNullOrEmpty(addressL2L3)) {
      lines.add(setN4DSLine("S20.G01.00.009.001", addressL2L3));
    }
    if (!Strings.isNullOrEmpty(address.getAddressL4())) {
      lines.add(setN4DSLine("S20.G01.00.009.006", address.getAddressL4()));
    }
    if (alpha2code.equals("FR")) {
      lines.add(setN4DSLine("S20.G01.00.009.010", address.getCity().getZip()));
      lines.add(setN4DSLine("S20.G01.00.009.012", address.getCity().getName()));
    } else {
      lines.add(setN4DSLine("S20.G01.00.009.013", alpha2code));
    }
    // TODO Identité du destinataire
    //	  lines.add(setN4DSLine("S20.G01.00.009.017",null));
    // TODO Numéro d'ordre de la déclaration
    lines.add(setN4DSLine("S20.G01.00.013.002","12"));
    lines.add(setN4DSLine("S20.G01.00.018", "A00"));

    // S70.G05.00
    lines.add(setN4DSLine("S70.G05.00.001", nic));
    lines.add(
        setN4DSLine(
            "S70.G05.00.002",
            accountingExport
            .getDateTo()
            .format(DateTimeFormatter.ofPattern(DATE_FORMAT_DDMMYYYY))));

    List<Object[]> dataList = getN4DSDeclaredPartnersData(accountingExport);
    for (Object[] listObj : dataList) {
      // S70.G10.00 Bénéficiaire des honoraires
      lines.add(setN4DSLine("S70.G10.00.001", listObj[0].toString()));
      String title = listObj[1].toString();
      String countryAlpha2code = listObj[10].toString();
      if (title.equals(String.valueOf(PartnerRepository.PARTNER_TYPE_COMPANY))) {
        String declarantRegistrationCode = listObj[4].toString().replaceAll(" ", "");

        String declarantSiren = computeSiren(declarantRegistrationCode, countryAlpha2code);
        String declarantNic = computeNic(declarantRegistrationCode, countryAlpha2code);
        lines.add(setN4DSLine("S70.G10.00.003.001", declarantSiren));
        lines.add(setN4DSLine("S70.G10.00.003.002", declarantNic));
        lines.add(setN4DSLine("S70.G10.00.003.003", listObj[2].toString()));
      } else {
        lines.add(setN4DSLine("S70.G10.00.002.001", listObj[2].toString()));
        lines.add(setN4DSLine("S70.G10.00.002.002", listObj[3].toString()));
      }
      if (listObj[5] != null && !Strings.isNullOrEmpty(listObj[5].toString())) {
        lines.add(setN4DSLine("S70.G10.00.004.001", listObj[6].toString()));
      }
      if (listObj[6] != null && !Strings.isNullOrEmpty(listObj[6].toString())) {
        lines.add(setN4DSLine("S70.G10.00.004.006", listObj[6].toString()));
      }
      if (countryAlpha2code.equals("FR")) {
        lines.add(setN4DSLine("S70.G10.00.004.010", listObj[8].toString()));
        lines.add(setN4DSLine("S70.G10.00.004.012", listObj[9].toString()));
      } else {
        lines.add(setN4DSLine("S70.G10.00.004.013", countryAlpha2code));
      }
      String serviceTypeCode = listObj[12].toString();
      String amount = listObj[13].toString();
      String serviceType = manageDAS2ServiceTypeToDeclare(serviceTypeCode);
      if (serviceType.equals("ANAT")) {
        // TODO quel code avantage mettre ?
        // S70.G10.05 Avantages en nature
        lines.add(setN4DSLine("S70.G10.05.001", "09"));
        lines.add(setN4DSLine("S70.G10.05.002", amount));
      } else if (serviceType.equals("IDTR")) {
        // TODO quel code mettre ?
        // S70.G10.10 Prise en charge des indemnités
        lines.add(setN4DSLine("S70.G10.10.001", "02"));
        lines.add(setN4DSLine("S70.G10.10.001", amount));
      } else {
        // S70.G10.15 Rémunérations
        lines.add(setN4DSLine("S70.G10.15.001", serviceType));
        lines.add(setN4DSLine("S70.G10.15.001", amount));
      }
    }
    // S80.G01.00
    lines.add(setN4DSLine("S80.G01.00.001.002", nic));
    lines.add(setN4DSLine("S80.G01.00.002", companyPartner.getName()));
    if (!Strings.isNullOrEmpty(addressL2L3)) {
      lines.add(setN4DSLine("S80.G01.00.003.001", addressL2L3));
    }
    if (!Strings.isNullOrEmpty(address.getAddressL4())) {
      lines.add(setN4DSLine("S80.G01.00.003.006", address.getAddressL4()));
    }
    if (alpha2code.equals("FR")) {
      lines.add(setN4DSLine("S80.G01.00.003.010", address.getCity().getZip()));
      lines.add(setN4DSLine("S80.G01.00.003.012", address.getCity().getName()));
    } else {
      lines.add(setN4DSLine("S80.G01.00.003.013", alpha2code));
    }
    lines.add(setN4DSLine("S80.G01.00.006", companyPartner.getMainActivityCode()));
    lines.add(setN4DSLine("S90.G01.00.001", String.valueOf(lines.size() + 2)));
    lines.add(setN4DSLine("S90.G01.00.002", "1"));

    return lines;
  }

  @Override
  public String setN4DSLine(String heading, String value) {

    return heading + ",'" + value + "'";
  }

  @Override
  public String computeSiren(String registrationCode, String countryAlpha2Code) {

    if (Strings.isNullOrEmpty(registrationCode) || Strings.isNullOrEmpty(countryAlpha2Code)) {
      return null;
    }

    if (countryAlpha2Code.equals("PF") || countryAlpha2Code.equals("MC")) {
      return registrationCode.substring(0, 6);
    }

    return registrationCode.substring(0, 9);
  }

  @Override
  public String computeNic(String registrationCode, String countryAlpha2Code) {

    if (Strings.isNullOrEmpty(registrationCode) || Strings.isNullOrEmpty(countryAlpha2Code)) {
      return null;
    }

    if (countryAlpha2Code.equals("PF") || countryAlpha2Code.equals("MC")) {
      return registrationCode.substring(registrationCode.length() - 3);
    }

    return registrationCode.substring(registrationCode.length() - 5);
  }

  @Override
  public String manageDAS2ServiceTypeToDeclare(String serviceTypeCode) {

    switch (serviceTypeCode) {
      case "HONO":
        return "01";
      case "COMM":
        return "02";
      case "COUR":
        return "03";
      case "RIST":
        return "04";
      case "JETO":
        return "05";
      case "DAUT":
        return "06";
      case "DINV":
        return "07";
      case "AREM":
        return "08";
      case "RSOU":
        return "11";
      case "FRAI":
        // TODO
        return null;
      default:
        return serviceTypeCode;
    }
  }

  @SuppressWarnings("unchecked")
@Override
  public List<Object[]> getN4DSDeclaredPartnersData(AccountingReport accountingExport) {

    String queryStr =
        "SELECT "
            + "MOVELINE.DAS2ACTIVITY_NAME AS ACTIVITY, "
            + "PARTNER.PARTNER_TYPE_SELECT AS TYPE, "
            + "PARTNER.NAME AS NAME, "
            + "PARTNER.FIRST_NAME AS FIRST_NAME, "
            + "PARTNER.REGISTRATION_CODE as REGISTRATION_CODE, "
            + "TRIM(CONCAT(ADDRESS.ADDRESSL2,' ',ADDRESS.ADDRESSL3)) AS ADDRESS_CONSTRUCTION, "
            + "ADDRESS.ADDRESSL4 AS ADDRESSL4, "
            + "ADDRESS.ADDRESSL5 as ADDRESSL5, "
            + "ADDRESS.ZIP AS ZIP, "
            + "CITY.NAME AS CITY, "
            + "COUNTRY.ALPHA2CODE AS COUNTRY, "
            + "COUNTRY.COG AS COG, "
            + "MOVELINE.SERVICE_TYPE_CODE AS SERVICE_TYPE, "
            + "SUM(TMOVELINE.DETAIL_PAYMENT_AMOUNT) AS AMOUNT "
            + "FROM ACCOUNT_ACCOUNTING_REPORT_MOVE_LINE T "
            + "JOIN ACCOUNT_TAX_PAYMENT_MOVE_LINE TMOVELINE ON T.TAX_PAYMENT_MOVE_LINE = TMOVELINE.ID "
            + "JOIN ACCOUNT_RECONCILE RECONCILE ON (TMOVELINE.RECONCILE = RECONCILE.ID) "
            + "LEFT OUTER JOIN ACCOUNT_MOVE_LINE MOVELINE ON (RECONCILE.CREDIT_MOVE_LINE = MOVELINE.ID) "
            + "LEFT OUTER JOIN ACCOUNT_SERVICE_TYPE AS SERVICETYPE ON (MOVELINE.SERVICE_TYPE = SERVICETYPE.ID) "
            + "LEFT OUTER JOIN BASE_PARTNER PARTNER ON (MOVELINE.PARTNER = PARTNER.ID) "
            + "LEFT OUTER JOIN BASE_ADDRESS AS ADDRESS ON (PARTNER.MAIN_ADDRESS = ADDRESS.ID) "
            + "LEFT OUTER JOIN BASE_COUNTRY AS COUNTRY ON (ADDRESS.ADDRESSL7COUNTRY = COUNTRY.ID) "
            + "LEFT OUTER JOIN BASE_CITY AS CITY ON (ADDRESS.CITY = CITY.ID) "
            + "WHERE T.ACCOUNTING_EXPORT = "
            + accountingExport.getId()
            + " AND T.EXCLUDE_FROM_DAS2REPORT != true "
            + "AND SERVICETYPE.IS_DAS2DECLARABLE = true "
            + "GROUP BY PARTNER_TYPE_SELECT,DAS2ACTIVITY_NAME,PARTNER.NAME,PARTNER.FIRST_NAME,PARTNER.FIRST_NAME,PARTNER.REGISTRATION_CODE, "
            + "ADDRESS.ADDRESSL2,ADDRESS.ADDRESSL3,ADDRESS.ADDRESSL4,ADDRESS.ADDRESSL5,ADDRESS.ZIP,CITY.NAME,COUNTRY.ALPHA2CODE,COUNTRY.COG,MOVELINE.SERVICE_TYPE_CODE,SERVICETYPE.NAME";

    Query query = JPA.em().createNativeQuery(queryStr);
    return query.getResultList();
  }

  @Override
  @Transactional
  public void updateN4DSExportStatus(AccountingReport accountingExport) {

    int i = 0;
    List<AccountingReportMoveLine> accountingReportMoveLines =
        getDasToDeclareLinesFromAccountingExport(accountingExport);
    if (CollectionUtils.isEmpty(accountingReportMoveLines)) {
      return;
    }

    for (AccountingReportMoveLine accountingReportMoveLine : accountingReportMoveLines) {
      accountingReportMoveLineRepo.find(accountingReportMoveLine.getId());
      accountingReportMoveLine.setExported(true);
      accountingReportMoveLineRepo.save(accountingReportMoveLine);
      i++;
      if (i % 10 == 0) {
        JPA.clear();
      }
    }
  }
}
