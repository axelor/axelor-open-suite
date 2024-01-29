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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.AccountingReportMoveLine;
import com.axelor.apps.account.db.PaymentMoveLineDistribution;
import com.axelor.apps.account.db.repo.AccountingReportMoveLineRepository;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.db.repo.PaymentMoveLineDistributionRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.studio.app.service.AppService;
import com.axelor.utils.file.FileTool;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.Query;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public class AccountingReportMoveLineServiceImpl implements AccountingReportMoveLineService {

  protected static final String DATE_FORMAT_DDMMYYYY = "ddMMyyyy";

  protected AccountingReportMoveLineRepository accountingReportMoveLineRepo;

  protected AccountingReportRepository accountingReportRepo;

  protected PaymentMoveLineDistributionRepository paymentMoveLineDistributionRepo;

  protected AppAccountService appAccountService;

  protected AccountConfigService accountConfigService;

  protected PartnerRepository partnerRepository;

  @Inject
  public AccountingReportMoveLineServiceImpl(
      AccountingReportMoveLineRepository accountingReportMoveLineRepo,
      AccountingReportRepository accountingReportRepo,
      PaymentMoveLineDistributionRepository paymentMoveLineDistributionRepo,
      AppAccountService appAccountService,
      AccountConfigService accountConfigService,
      PartnerRepository partnerRepository) {
    this.accountingReportMoveLineRepo = accountingReportMoveLineRepo;
    this.paymentMoveLineDistributionRepo = paymentMoveLineDistributionRepo;
    this.accountingReportRepo = accountingReportRepo;
    this.appAccountService = appAccountService;
    this.accountConfigService = accountConfigService;
    this.partnerRepository = partnerRepository;
  }

  @Override
  public void createAccountingReportMoveLines(
      List<Long> paymentMoveLineDistributioneIds, AccountingReport accountingReport) {

    for (Long id : paymentMoveLineDistributioneIds) {
      PaymentMoveLineDistribution paymentMoveLineDistribution =
          paymentMoveLineDistributionRepo.find(id);
      if (paymentMoveLineDistribution != null) {
        createAccountingReportMoveLine(
            paymentMoveLineDistribution, accountingReportRepo.find(accountingReport.getId()));
      }
    }
  }

  @Transactional
  @Override
  public void createAccountingReportMoveLine(
      PaymentMoveLineDistribution paymentMoveLineDistribution, AccountingReport accountingReport) {

    AccountingReportMoveLine accountingReportMoveLine =
        new AccountingReportMoveLine(paymentMoveLineDistribution, accountingReport);
    accountingReportMoveLine.setExcludeFromDas2Report(
        paymentMoveLineDistribution.getExcludeFromDas2Report());
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
      AccountingReport accountingExport) {

    List<Long> partnerIdList =
        JPA.em()
            .createQuery(
                "SELECT DISTINCT(self.paymentMoveLineDistribution.partner.id) FROM AccountingReportMoveLine self "
                    + "WHERE self.accountingExport.id = :accountingExportId"
                    + " AND self.excludeFromDas2Report != true "
                    + " AND self.exported != true ",
                Long.class)
            .setParameter("accountingExportId", accountingExport.getId())
            .getResultList();

    if (partnerIdList.isEmpty()) {
      return new ArrayList<>();
    } else {
      return partnerRepository
          .all()
          .filter("self.id IN (:idList)")
          .bind("idList", partnerIdList)
          .fetch();
    }
  }

  @Override
  public List<AccountingReportMoveLine> getDasToDeclareLinesFromAccountingExport(
      AccountingReport accountingExport) {

    return accountingReportMoveLineRepo
        .all()
        .filter(
            "self.accountingExport = ?1 AND self.excludeFromDas2Report != true "
                + "AND self.exported != true AND self.paymentMoveLineDistribution.moveLine.account.serviceType.isDas2Declarable != true AND self.paymentMoveLineDistribution.moveLine.account.serviceType.n4dsCode is null",
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
    return Beans.get(MetaFiles.class).attach(is, fileName, accountingExport).getMetaFile();
  }

  protected String compileStringValue(String regex, String value, String replaceString) {

    value = StringUtils.stripAccents(value);
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(value);
    return matcher.replaceAll(replaceString);
  }

  /**
   * Replace unauthorized characters for identity fields
   *
   * @param value
   * @return value
   */
  protected String compileIdentityValue(String value) {

    String regex =
        "[0-9!\\\"#$%&()*+,./:;<=>?\\@\\[\\\\\\]_`\\{|\\}~¡¢£¤¥¦§©«»¬®°±μ¶·¿ÆÐ×ØÞßð÷øþ^]";

    return compileStringValue(regex, value, "");
  }

  /**
   * Replace unauthorized characters for address fields
   *
   * @param value
   * @return value
   */
  protected String compileAddressValue(String value) {

    String regex = "[!\\\"#$%&()*+,./:;<=>?\\@\\[\\\\\\]_`\\{|\\}~¡¢£¤¥¦§©«»¬®°±μ¶·¿ÆÐ×ØÞßð÷øþ^]";

    return compileStringValue(regex, value, "");
  }

  @Override
  public List<String> generateN4DSLines(AccountingReport accountingExport) throws AxelorException {

    List<String> lines = new ArrayList<>();

    Partner companyPartner = accountingExport.getCompany().getPartner();
    Address address = companyPartner.getMainAddress();
    String alpha2code = address.getAddressL7Country().getAlpha2Code();
    String registrationCode = companyPartner.getRegistrationCode().replaceAll(" ", "");
    String siren = computeSiren(registrationCode, alpha2code);
    String nic = computeNic(registrationCode, alpha2code);

    String regexForCity = "[^0-9a-zA-Z_\\s]";

    // S10.G10.00
    lines.add(setN4DSLine("S10.G01.00.001.001", siren));
    lines.add(setN4DSLine("S10.G01.00.001.002", nic));
    lines.add(setN4DSLine("S10.G01.00.002", compileIdentityValue(companyPartner.getName())));
    String addressL2L3 = null;
    if (!Strings.isNullOrEmpty(address.getAddressL2())) {
      addressL2L3 = address.getAddressL2().trim();
    }
    if (!Strings.isNullOrEmpty(address.getAddressL3())) {
      if (Strings.isNullOrEmpty(addressL2L3)) {
        addressL2L3 = compileAddressValue(address.getAddressL3().trim());
      } else {
        addressL2L3 = compileAddressValue(addressL2L3 + " " + address.getAddressL3().trim());
      }
    }
    if (!Strings.isNullOrEmpty(addressL2L3)) {
      lines.add(setN4DSLine("S10.G01.00.003.001", addressL2L3));
    }
    if (!Strings.isNullOrEmpty(address.getAddressL4())) {
      lines.add(setN4DSLine("S10.G01.00.003.006", compileAddressValue(address.getAddressL4())));
    }
    if (alpha2code.equals("FR")) {
      lines.add(setN4DSLine("S10.G01.00.003.010", compileAddressValue(address.getCity().getZip())));
      lines.add(
          setN4DSLine(
              "S10.G01.00.003.012",
              compileStringValue(regexForCity, address.getCity().getName(), " ")));
    } else {
      lines.add(setN4DSLine("S10.G01.00.003.013", alpha2code));
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
    lines.add(
        setN4DSLine(
            "S10.G01.01.001.002", compileIdentityValue(dasContactPartner.getSimpleFullName())));
    lines.add(setN4DSLine("S10.G01.01.002", "01"));
    lines.add(setN4DSLine("S10.G01.01.005", dasContactPartner.getEmailAddress().getAddress()));
    lines.add(
        setN4DSLine(
            "S10.G01.01.006",
            ObjectUtils.firstNonNull(
                dasContactPartner.getFixedPhone(), dasContactPartner.getMobilePhone())));

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
        setN4DSLine("S20.G01.00.004.002", accountingExport.getComplementaryExport() ? "52" : "51"));
    lines.add(setN4DSLine("S20.G01.00.005", "11"));
    if (accountingExport.getComplementaryExport()) {
      lines.add(
          setN4DSLine(
              "S20.G01.00.006.001",
              accountingExport
                  .getDateFrom()
                  .format(DateTimeFormatter.ofPattern(DATE_FORMAT_DDMMYYYY))));
      lines.add(
          setN4DSLine(
              "S20.G01.00.006.002",
              accountingExport
                  .getDateTo()
                  .format(DateTimeFormatter.ofPattern(DATE_FORMAT_DDMMYYYY))));
    }
    lines.add(setN4DSLine("S20.G01.00.007", "01"));
    lines.add(setN4DSLine("S20.G01.00.008", nic));
    if (!Strings.isNullOrEmpty(addressL2L3)) {
      lines.add(setN4DSLine("S20.G01.00.009.001", addressL2L3));
    }
    if (!Strings.isNullOrEmpty(address.getAddressL4())) {
      lines.add(setN4DSLine("S20.G01.00.009.006", compileAddressValue(address.getAddressL4())));
    }
    if (alpha2code.equals("FR")) {
      lines.add(setN4DSLine("S20.G01.00.009.010", compileAddressValue(address.getCity().getZip())));
      lines.add(
          setN4DSLine(
              "S20.G01.00.009.012",
              compileStringValue(regexForCity, address.getCity().getName(), " ")));
    } else {
      lines.add(setN4DSLine("S20.G01.00.009.013", alpha2code));
    }
    lines.add(setN4DSLine("S20.G01.00.013.002", "12"));
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
      lines.add(
          setN4DSLine(
              "S70.G10.00.001", StringUtils.stripAccents(listObj[0].toString().toUpperCase())));
      String title = listObj[1].toString();
      String countryAlpha2code = listObj[9].toString();
      String zip = "0";
      if (listObj[7] != null) {
        zip = compileAddressValue(listObj[7].toString());
        zip = StringUtils.isEmpty(zip) ? "0" : zip;
      }

      if (title.equals(String.valueOf(PartnerRepository.PARTNER_TYPE_COMPANY))) {
        String declarantRegistrationCode = listObj[4].toString().replaceAll(" ", "");

        String declarantSiren = computeSiren(declarantRegistrationCode, countryAlpha2code);
        String declarantNic = computeNic(declarantRegistrationCode, countryAlpha2code);
        lines.add(setN4DSLine("S70.G10.00.003.001", declarantSiren));
        lines.add(setN4DSLine("S70.G10.00.003.002", declarantNic));
        lines.add(setN4DSLine("S70.G10.00.003.003", listObj[2].toString()));
      } else {
        lines.add(setN4DSLine("S70.G10.00.002.001", compileIdentityValue(listObj[2].toString())));
        lines.add(setN4DSLine("S70.G10.00.002.002", compileIdentityValue(listObj[3].toString())));
      }
      if (listObj[5] != null && !Strings.isNullOrEmpty(listObj[5].toString())) {
        lines.add(setN4DSLine("S70.G10.00.004.001", compileAddressValue(listObj[5].toString())));
      }
      if (listObj[6] != null && !Strings.isNullOrEmpty(listObj[6].toString())) {
        lines.add(setN4DSLine("S70.G10.00.004.006", compileAddressValue(listObj[6].toString())));
      }
      if (countryAlpha2code.equals("FR")) {
        lines.add(setN4DSLine("S70.G10.00.004.010", zip));
        lines.add(
            setN4DSLine(
                "S70.G10.00.004.012",
                compileStringValue(regexForCity, listObj[8].toString(), " ")));
      } else {
        lines.add(setN4DSLine("S70.G10.00.004.013", countryAlpha2code));
        lines.add(setN4DSLine("S70.G10.00.004.016", zip));
      }
      String serviceTypeCode = listObj[10].toString();
      String amount = listObj[11].toString();
      // S70.G10.15 Rémunérations
      lines.add(setN4DSLine("S70.G10.15.001", serviceTypeCode));
      lines.add(setN4DSLine("S70.G10.15.002", amount));
    }
    // S80.G01.00
    lines.add(setN4DSLine("S80.G01.00.001.002", nic));
    lines.add(setN4DSLine("S80.G01.00.002", companyPartner.getName()));
    if (!Strings.isNullOrEmpty(addressL2L3)) {
      lines.add(setN4DSLine("S80.G01.00.003.001", addressL2L3));
    }
    if (!Strings.isNullOrEmpty(address.getAddressL4())) {
      lines.add(setN4DSLine("S80.G01.00.003.006", compileAddressValue(address.getAddressL4())));
    }
    if (alpha2code.equals("FR")) {
      lines.add(setN4DSLine("S80.G01.00.003.010", compileAddressValue(address.getCity().getZip())));
      lines.add(
          setN4DSLine(
              "S80.G01.00.003.012",
              compileStringValue(regexForCity, address.getCity().getName(), " ")));
    } else {
      lines.add(setN4DSLine("S80.G01.00.003.013", alpha2code));
    }
    lines.add(setN4DSLine("S80.G01.00.005", "02"));
    lines.add(setN4DSLine("S80.G01.00.006", companyPartner.getMainActivity().getCode()));
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

  @SuppressWarnings("unchecked")
  @Override
  public List<Object[]> getN4DSDeclaredPartnersData(AccountingReport accountingExport) {

    String queryStr =
        "SELECT "
            + "partner.das2Activity.name AS ACTIVITY, "
            + "partner.partnerTypeSelect AS TYPE, "
            + "partner.name AS NAME, "
            + "partner.firstName AS FIRST_NAME, "
            + "partner.registrationCode as REGISTRATION_CODE, "
            + "TRIM(CONCAT(address.addressL2,' ',address.addressL3)) AS ADDRESS_CONSTRUCTION, "
            + "address.addressL4 AS ADDRESSL4, "
            + "city.zip AS ZIP, "
            + "city.name AS CITY, "
            + "country.alpha2Code AS COUNTRY, "
            + "serviceType.n4dsCode AS SERVICE_TYPE, "
            + "SUM(pmvld.inTaxProratedAmount) AS AMOUNT  "
            + "FROM AccountingReportMoveLine history "
            + "JOIN history.paymentMoveLineDistribution pmvld "
            + "LEFT OUTER JOIN pmvld.moveLine moveLine "
            + "LEFT OUTER JOIN pmvld.partner partner "
            + "LEFT OUTER JOIN partner.mainAddress address "
            + "LEFT OUTER JOIN address.addressL7Country country "
            + "LEFT OUTER JOIN address.city city "
            + "LEFT OUTER JOIN moveLine.account account "
            + "LEFT OUTER JOIN account.serviceType serviceType "
            + "WHERE history.accountingExport =  :accountingExport "
            + "AND history.excludeFromDas2Report != true  "
            + "AND serviceType.isDas2Declarable = true  "
            + "AND serviceType.n4dsCode IS NOT NULL "
            + "GROUP BY partner.partnerTypeSelect,partner.das2Activity.name,"
            + "partner.name,partner.firstName,partner.registrationCode, "
            + "address.addressL2,address.addressL3,address.addressL4,"
            + "city.zip,city.name,country.alpha2Code,serviceType.n4dsCode";

    Query query = JPA.em().createQuery(queryStr).setParameter("accountingExport", accountingExport);
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
