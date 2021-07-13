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

import java.math.BigInteger;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import com.axelor.app.AppSettings;
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
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFile;
import com.google.api.client.repackaged.com.google.common.base.Strings;
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

	List<AccountingReportMoveLine> reportMoveLines = getDasToDeclareLinesFromAccountingExport(accountingExport);
			
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
  public List<AccountingReportMoveLine> getDasToDeclareLinesFromAccountingExport(AccountingReport accountingExport) {
	  
	  return accountingReportMoveLineRepo
		            .all()
		            .filter(
		                "self.accountingExport = ?1 AND self.excludeFromDas2Report != true AND self.exported != true",
		                accountingExport)
		            .fetch();
  }

  @Override
  public MetaFile generateN4DSFile(AccountingReport accountingExport, String fileName) throws AxelorException {
	  
	  List<String> Lines = Lists.newArrayList();
	  Lines.addAll(generateN4DSLines(accountingExport));
	  
//	Beans.get(MetaFiles.class).attach(is, fileName, accountingExport).getMetaFile();
	return null;
  }
  
  @Override
  public List<String> generateN4DSLines(AccountingReport accountingExport) throws AxelorException {
	  
	  List<String> lines = Lists.newArrayList();
	  
	  Partner companyPartner = accountingExport.getCompany().getPartner();
	  Address address = companyPartner.getMainAddress();
	  String alpha2code = address.getAddressL7Country().getAlpha2Code();
	  String siren = computeSiren(companyPartner.getRegistrationCode(), alpha2code);
	  String nic = computeNic(companyPartner.getRegistrationCode(), alpha2code);
	  
	  //TODO regex identités et adresses
	  
	  //S10.G10.00
	  lines.add(setN4DSLine("S10.G01.00.001.001",siren));
	  lines.add(setN4DSLine("S10.G01.00.001.002",nic));
	  lines.add(setN4DSLine("S10.G01.00.002",companyPartner.getName()));
	  String addressL2L3 =  (address.getAddressL2() + " " + address.getAddressL3()).trim();
	  if (!Strings.isNullOrEmpty(addressL2L3)) {
		  lines.add(setN4DSLine("S10.G01.00.003.001",addressL2L3));
	  }
	  if (!Strings.isNullOrEmpty(address.getAddressL4())) {
		  lines.add(setN4DSLine("S10.G01.00.003.006",address.getAddressL4()));
	  }
	  if (!Strings.isNullOrEmpty(address.getAddressL5())) {
		  lines.add(setN4DSLine("S10.G01.00.003.009",address.getAddressL5()));
	  }
	  if (alpha2code.equals("FR") ) {
		  lines.add(setN4DSLine("S10.G01.00.003.010",address.getZip()));
		  lines.add(setN4DSLine("S10.G01.00.003.012",address.getCity().getName()));
	  }else {
		  lines.add(setN4DSLine("S10.G01.00.003.013",alpha2code));
		  lines.add(setN4DSLine("S10.G01.00.003.016",address.getAddressL7Country().getCog()));
	  }
	  //TODO Identité du destinataire
	  lines.add(setN4DSLine("S10.G01.00.003.017",null));
	  lines.add(setN4DSLine("S10.G01.00.004","0"));
	  lines.add(setN4DSLine("S10.G01.00.005","Axelor Open Suite"));
	  lines.add(setN4DSLine("S10.G01.00.006","Axelor"));
	  lines.add(setN4DSLine("S10.G01.00.005",AppSettings.get().get("application.version")));
	  //TODO Code du logiciel de pré-contrôle
	  lines.add(setN4DSLine("S10.G01.00.008",null));
	  lines.add(setN4DSLine("S10.G01.00.009","40"));
	  lines.add(setN4DSLine("S10.G01.00.010","02"));
	  lines.add(setN4DSLine("S10.G01.00.011",appAccountService.getAppAccount().getDasActiveNorm()));
	  lines.add(setN4DSLine("S10.G01.00.012","01"));
	  
	  //S10.G01.01
	  Partner dasContactPartner = accountConfigService.getAccountConfig(accountingExport.getCompany()).getDasContactPartner();
	  lines.add(setN4DSLine("S10.G01.01.001.001",dasContactPartner.getTitleSelect().equals(PartnerRepository.PARTNER_TITLE_MS) ? "02" : "01"));
	  lines.add(setN4DSLine("S10.G01.01.001.002",dasContactPartner.getSimpleFullName()));
	  lines.add(setN4DSLine("S10.G01.01.002","01"));
	  lines.add(setN4DSLine("S10.G01.01.005",dasContactPartner.getEmailAddress().getAddress()));
	  lines.add(setN4DSLine("S10.G01.01.006",ObjectUtils.firstNonNull(dasContactPartner.getFixedPhone(),dasContactPartner.getMobilePhone())));
	  if (!Strings.isNullOrEmpty(dasContactPartner.getFax())) {
		  lines.add(setN4DSLine("S10.G01.01.007",dasContactPartner.getFax()));
	  }
	  
	  //S10.G01.05
	  lines.add(setN4DSLine("S10.G01.05.013.001",siren));
	  lines.add(setN4DSLine("S10.G01.05.013.002",nic));
	  lines.add(setN4DSLine("S10.G01.05.015.001",dasContactPartner.getEmailAddress().getAddress()));
	  
	  //S20.G01.00
	  lines.add(setN4DSLine("S20.G01.00.001",siren));
	  lines.add(setN4DSLine("S20.G01.00.002",companyPartner.getName()));
	  lines.add(setN4DSLine("S20.G01.00.003.001",accountingExport.getDateFrom().format(DateTimeFormatter.ofPattern(DATE_FORMAT_DDMMYYYY))));
	  lines.add(setN4DSLine("S20.G01.00.003.002",accountingExport.getDateTo().format(DateTimeFormatter.ofPattern(DATE_FORMAT_DDMMYYYY))));
	  lines.add(setN4DSLine("S20.G01.00.004.001","12"));
	  //TODO gérer le cas complémentaire : 52
	  lines.add(setN4DSLine("S20.G01.00.004.001","51"));
	  //TODO Numéro de fraction de déclaration
	  lines.add(setN4DSLine("S20.G01.00.005",null));
	  lines.add(setN4DSLine("S20.G01.00.006.001",accountingExport.getDateFrom().format(DateTimeFormatter.ofPattern(DATE_FORMAT_DDMMYYYY))));
	  lines.add(setN4DSLine("S20.G01.00.006.002",accountingExport.getDateTo().format(DateTimeFormatter.ofPattern(DATE_FORMAT_DDMMYYYY))));
	  lines.add(setN4DSLine("S20.G01.00.007","01"));
	  lines.add(setN4DSLine("S20.G01.00.008",nic));
	  if (!Strings.isNullOrEmpty(addressL2L3)) {
		  lines.add(setN4DSLine("S20.G01.00.009.001",addressL2L3));
	  }
	  if (!Strings.isNullOrEmpty(address.getAddressL4())) {
		  lines.add(setN4DSLine("S20.G01.00.009.006",address.getAddressL4()));
	  }
	  if (!Strings.isNullOrEmpty(address.getAddressL5())) {
		  lines.add(setN4DSLine("S20.G01.00.009.009",address.getAddressL5()));
	  }
	  if (alpha2code.equals("FR") ) {
		  lines.add(setN4DSLine("S20.G01.00.009.010",address.getZip()));
		  lines.add(setN4DSLine("S20.G01.00.009.012",address.getCity().getName()));
	  }else {
		  lines.add(setN4DSLine("S20.G01.00.009.013",alpha2code));
		  lines.add(setN4DSLine("S20.G01.00.009.016",address.getAddressL7Country().getCog()));
	  }
	  //TODO Identité du destinataire
	  lines.add(setN4DSLine("S20.G01.00.009.017",null));
	  //TODO Numéro d'ordre de la déclaration
	  lines.add(setN4DSLine("S20.G01.00.013.002",null));
	  lines.add(setN4DSLine("S20.G01.00.018","A00"));
	  
	  //S70.G05.00
	  lines.add(setN4DSLine("S70.G05.00.001",nic));
	  lines.add(setN4DSLine("S70.G05.00.002",accountingExport.getYear().getClosureDateTime().format(DateTimeFormatter.ofPattern(DATE_FORMAT_DDMMYYYY))));
	  
	  //TODO partnersToDeclare
	  List<List> dataList = getN4DSDeclaredPartnersData(accountingExport);
	  for (List listObj : dataList) {
          lines.add(setN4DSLine("S70.G10.00.001",listObj.get(0).toString()));
          String title = listObj.get(1).toString();
          String countryAlpha2code = listObj.get(11).toString(); 
          if (title.equals(PartnerRepository.PARTNER_TYPE_COMPANY)) {
        	  lines.add(setN4DSLine("S70.G10.00.003.001",listObj.get(0).toString()));
        	  lines.add(setN4DSLine("S70.G10.00.003.002",listObj.get(0).toString()));
        	  
          }
          lines.add(setN4DSLine("S70.G10.00.001",listObj.get(0).toString()));
	  }
	  //S70.G10.00 Bénéficiaire des honoraires
	  //S70.G10.05 Avantages en nature
	  //S70.G10.10 Prise en charge des indemnités
	  //S70.G10.15 Rémunérations
	  
	  //S80.G01.00
	  lines.add(setN4DSLine("S80.G01.00.001.002",nic));
	  lines.add(setN4DSLine("S80.G01.00.002",companyPartner.getName()));
	  if (!Strings.isNullOrEmpty(addressL2L3)) {
		  lines.add(setN4DSLine("S80.G01.00.003.001",addressL2L3));
	  }
	  if (!Strings.isNullOrEmpty(address.getAddressL4())) {
		  lines.add(setN4DSLine("S80.G01.00.003.006",address.getAddressL4()));
	  }
	  if (!Strings.isNullOrEmpty(address.getAddressL5())) {
		  lines.add(setN4DSLine("S80.G01.00.003.009",address.getAddressL5()));
	  }
	  if (alpha2code.equals("FR") ) {
		  lines.add(setN4DSLine("S80.G01.00.003.010",address.getZip()));
		  lines.add(setN4DSLine("S80.G01.00.003.012",address.getCity().getName()));
	  }else {
		  lines.add(setN4DSLine("S80.G01.00.003.013",alpha2code));
		  lines.add(setN4DSLine("S80.G01.00.003.016",address.getAddressL7Country().getCog()));
	  }
	  //TODO Identité du destinataire
	  lines.add(setN4DSLine("S80.G01.00.003.017",null));
	  lines.add(setN4DSLine("S80.G01.00.006",companyPartner.getMainActivityCode()));
	  lines.add(setN4DSLine("S90.G01.00.001",String.valueOf(lines.size()+ 2)));
	  lines.add(setN4DSLine("S90.G01.00.002","1"));
	  
	  return lines;
  }
  
  public String setN4DSLine(String heading, String value) {
	  
	  return heading + ",'"+ value + "'"; 
  }
  
  public String computeSiren(String registrationCode, String countryAlpha2Code) {
	  
	  if (Strings.isNullOrEmpty(registrationCode) || Strings.isNullOrEmpty(countryAlpha2Code)) { return null; }
	  
	  if (countryAlpha2Code.equals("PF") || countryAlpha2Code.equals("MC")) { return registrationCode.substring(0,5); }
	  
	  return registrationCode.substring(0,8);
	  
  }
  
  public String computeNic(String registrationCode, String countryAlpha2Code) {
	  
	  if (Strings.isNullOrEmpty(registrationCode) || Strings.isNullOrEmpty(countryAlpha2Code)) { return null; }
	  
	  if (countryAlpha2Code.equals("PF") || countryAlpha2Code.equals("MC")) { return registrationCode.substring(registrationCode.length()-3); }
	  
	  return registrationCode.substring(registrationCode.length()-5);
	  
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public List<List> getN4DSDeclaredPartnersData(AccountingReport accountingExport) {
	
	  String queryStr = "SELECT "
	  		+ "MOVELINE.DAS2ACTIVITY_NAME AS ACTIVITY, "
	  		+ "PARTNER.PARTNER_TYPE_SELECT AS TYPE, "
	  		+ "PARTNER.NAME AS NAME, "
	  		+ "PARTNER.FIRST_NAME AS FIRST_NAME, "
	  		+ "PARTNER.SIREN as SIREN, "
	  		+ "PARTNER.NIC AS NIC, "
	  		+ "TRIM(CONCAT(ADDRESS.ADDRESSL2,' ',ADDRESS.ADDRESSL3)) AS ADDRESS_CONSTRUCTION, "
	  		+ "ADDRESS.ADDRESSL4 AS ADDRESSL4, "
	  		+ "ADDRESS.ADDRESSL5 as ADDRESSL5, "
	  		+ "ADDRESS.ZIP AS ZIP, "
	  		+ "CITY.NAME AS CITY, "
	  		+ "COUNTRY.ALPHA2CODE AS COUNTRY, "
	  		+ "COUNTRY.COG AS COG, "
	  		+ "MOVELINE.SERVICE_TYPE_CODE AS SERVICE_TYPE, "
	  		+ "SERVICETYPE.NAME as SERVICE_NAME, "
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
	  		+ "GROUP BY PARTNER_TYPE_SELECT,DAS2ACTIVITY_NAME,PARTNER.NAME,PARTNER.FIRST_NAME,PARTNER.FIRST_NAME,PARTNER.SIREN,PARTNER.NIC, "
	  		+ "ADDRESS.ADDRESSL2,ADDRESS.ADDRESSL3,ADDRESS.ADDRESSL4,ADDRESS.ADDRESSL5,ADDRESS.ZIP,CITY.NAME,COUNTRY.ALPHA2CODE,COUNTRY.COG,MOVELINE.SERVICE_TYPE_CODE,SERVICETYPE.NAME";
	  		
	  Query query = JPA.em().createNativeQuery(queryStr);
	  return query.getResultList();
  }
}
