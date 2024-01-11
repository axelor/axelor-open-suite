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

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class AccountingReportDas2CheckServiceImpl implements AccountingReportDas2CheckService {

  protected AccountConfigService accountConfigService;
  protected AccountingReportMoveLineService accountingReportMoveLineService;
  protected AppAccountService appAccountService;

  @Inject
  public AccountingReportDas2CheckServiceImpl(
      AccountConfigService accountConfigService,
      AccountingReportMoveLineService accountingReportMoveLineService,
      AppAccountService appAccountService) {
    this.accountConfigService = accountConfigService;
    this.accountingReportMoveLineService = accountingReportMoveLineService;
    this.appAccountService = appAccountService;
  }

  @Override
  public List<String> checkMandatoryDataForDas2Export(AccountingReport accountingExport)
      throws AxelorException {

    List<String> errorList = new ArrayList<>();

    errorList.addAll(checkDasToDeclarePartners(accountingExport));
    errorList.addAll(checkDasContactPartner(accountingExport));
    errorList.addAll(checkDasDeclarantCompany(accountingExport));

    return errorList;
  }

  protected List<String> checkDasContactPartner(AccountingReport accountingExport)
      throws AxelorException {

    AccountConfig accountConfig =
        accountConfigService.getAccountConfig(accountingExport.getCompany());
    Partner partner = accountConfig.getDasContactPartner();

    List<String> errorList = new ArrayList<>();

    if (partner == null) {
      errorList.add(I18n.get(AccountExceptionMessage.ACCOUNTING_REPORT_DAS2_CONTACT_MISSING));
      return errorList;
    }
    if (partner.getTitleSelect() == null) {
      errorList.add(I18n.get(AccountExceptionMessage.ACCOUNTING_REPORT_DAS2_CONTACT_TITLE_MISSING));
    } else if (!partner.getTitleSelect().equals(PartnerRepository.PARTNER_TITLE_M)
        && !partner.getTitleSelect().equals(PartnerRepository.PARTNER_TITLE_MS)) {
      errorList.add(I18n.get(AccountExceptionMessage.ACCOUNTING_REPORT_DAS2_CONTACT_WRONG_TITLE));
    }
    if (Strings.isNullOrEmpty(partner.getFirstName())) {
      errorList.add(
          I18n.get(AccountExceptionMessage.ACCOUNTING_REPORT_DAS2_CONTACT_FIRST_NAME_MISSING));
    }
    if (partner.getEmailAddress() == null
        || Strings.isNullOrEmpty(partner.getEmailAddress().getAddress())) {
      errorList.add(I18n.get(AccountExceptionMessage.ACCOUNTING_REPORT_DAS2_CONTACT_EMAIL_MISSING));
    }
    if (Strings.isNullOrEmpty(partner.getFixedPhone())
        && Strings.isNullOrEmpty(partner.getMobilePhone())) {
      errorList.add(I18n.get(AccountExceptionMessage.ACCOUNTING_REPORT_DAS2_CONTACT_PHONE_MISSING));
    }
    return errorList;
  }

  protected List<String> checkDasDeclarantCompany(AccountingReport accountingExport) {

    Partner companyPartner = accountingExport.getCompany().getPartner();
    String companyName = accountingExport.getCompany().getName();

    List<String> errorList = new ArrayList<>();

    if (companyPartner == null) {
      errorList.add(I18n.get(AccountExceptionMessage.ACCOUNTING_REPORT_MISSING_COMPANY_PARTNER));
      return errorList;
    }

    Address companyPartnerMainAddress = companyPartner.getMainAddress();

    if (companyPartnerMainAddress == null) {
      errorList.add(
          String.format(
              I18n.get(AccountExceptionMessage.ACCOUNTING_REPORT_MISSING_COMPANY_PARTNER_ADDRESS),
              companyName));
    } else if (companyPartnerMainAddress.getAddressL7Country() == null) {
      errorList.add(
          String.format(
              I18n.get(
                  AccountExceptionMessage.ACCOUNTING_REPORT_MISSING_COMPANY_PARTNER_ADDRESS_L7),
              companyName));
    } else {
      if (Strings.isNullOrEmpty(companyPartnerMainAddress.getAddressL7Country().getAlpha2Code())) {
        errorList.add(
            String.format(
                I18n.get(
                    AccountExceptionMessage
                        .ACCOUNTING_REPORT_MISSING_COMPANY_PARTNER_ADDRESS_L7_A2CODE),
                companyName));
      } else if ("FR".equals(companyPartnerMainAddress.getAddressL7Country().getAlpha2Code())) {
        if (companyPartnerMainAddress.getCity() == null) {
          errorList.add(
              String.format(
                  I18n.get(
                      AccountExceptionMessage
                          .ACCOUNTING_REPORT_MISSING_COMPANY_PARTNER_ADDRESS_CITY),
                  companyName));
        } else if (Strings.isNullOrEmpty(companyPartnerMainAddress.getCity().getZip())) {
          errorList.add(
              String.format(
                  I18n.get(
                      AccountExceptionMessage
                          .ACCOUNTING_REPORT_MISSING_COMPANY_PARTNER_ADDRESS_CITY_ZIP),
                  companyName));
        }
      }
    }

    if (Strings.isNullOrEmpty(appAccountService.getAppAccount().getDasActiveNorm())) {
      errorList.add(I18n.get(AccountExceptionMessage.ACCOUNTING_REPORT_DAS2_ACTIVE_NORM));
    }

    if (Strings.isNullOrEmpty(companyPartner.getRegistrationCode())) {
      errorList.add(
          String.format(
              I18n.get(
                  AccountExceptionMessage
                      .ACCOUNTING_REPORT_DAS2_DECLARANT_COMPANY_MISSING_REGISTRATION_CODE),
              companyName));
    }
    if (companyPartner.getMainActivity() == null
        || Strings.isNullOrEmpty(companyPartner.getMainActivity().getCode())) {
      errorList.add(
          String.format(
              I18n.get(
                  AccountExceptionMessage.ACCOUNTING_REPORT_DAS2_DECLARANT_COMPANY_MISSING_NAF),
              companyName));
    }

    return errorList;
  }

  protected List<String> checkDasToDeclarePartners(AccountingReport accountingExport) {

    List<Partner> partners =
        accountingReportMoveLineService.getDasToDeclarePartnersFromAccountingExport(
            accountingExport);
    List<String> errorList = new ArrayList<>();

    for (Partner partner : partners) {
      errorList.addAll(checkDasToDeclarePartner(partner));
    }
    return errorList;
  }

  protected List<String> checkDasToDeclarePartner(Partner partner) {

    List<String> errorList = new ArrayList<>();

    checkDasToDeclarePartnerMainAddress(partner, errorList);

    String partnerSeq = partner.getPartnerSeq();
    String partnerSimpleFullName = partner.getSimpleFullName();

    if (partner.getPartnerTypeSelect() != PartnerRepository.PARTNER_TYPE_COMPANY) {
      if (partner.getTitleSelect() == null) {
        errorList.add(
            String.format(
                I18n.get(
                    AccountExceptionMessage.ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_TITLE_MISSING),
                partnerSeq,
                partnerSimpleFullName));
      } else if (!partner.getTitleSelect().equals(PartnerRepository.PARTNER_TITLE_M)
          && !partner.getTitleSelect().equals(PartnerRepository.PARTNER_TITLE_MS)) {
        errorList.add(
            String.format(
                I18n.get(
                    AccountExceptionMessage.ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_WRONG_TITLE),
                partnerSeq,
                partnerSimpleFullName));
      }
      if (partner.getFirstName() == null) {
        errorList.add(
            String.format(
                I18n.get(
                    AccountExceptionMessage
                        .ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_FIRST_NAME_MISSING),
                partnerSeq,
                partnerSimpleFullName));
      }
    }
    return errorList;
  }

  protected void checkDasToDeclarePartnerMainAddress(Partner partner, List<String> errorList) {
    String partnerSeq = partner.getPartnerSeq();
    String partnerSimpleFullName = partner.getSimpleFullName();
    Address partnerMainAddress = partner.getMainAddress();

    if (partnerMainAddress == null) {
      errorList.add(
          String.format(
              I18n.get(
                  AccountExceptionMessage.ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_MISSING_ADDRESS),
              partnerSeq,
              partnerSimpleFullName));
      return;
    }

    if (partnerMainAddress.getAddressL7Country() != null
        && "FR".equals(partnerMainAddress.getAddressL7Country().getAlpha2Code())) {
      if (partnerMainAddress.getCity() == null) {
        errorList.add(
            String.format(
                I18n.get(
                    AccountExceptionMessage
                        .ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_MISSING_ADDRESS_CITY),
                partnerSeq,
                partnerSimpleFullName));
      } else if (partnerMainAddress.getCity().getZip() == null) {
        errorList.add(
            String.format(
                I18n.get(
                    AccountExceptionMessage
                        .ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_MISSING_ADDRESS_CITY_ZIP),
                partnerSeq,
                partnerSimpleFullName));
      }
    }

    if (partner.getPartnerTypeSelect() == PartnerRepository.PARTNER_TYPE_COMPANY) {

      if (!partnerMainAddress.getAddressL7Country().getAlpha2Code().equals("FR")) {
        errorList.add(
            String.format(
                I18n.get(
                    AccountExceptionMessage
                        .ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_INCONSISTENT_TITLE),
                partnerSeq,
                partnerSimpleFullName));
      }
      if (Strings.isNullOrEmpty(partner.getRegistrationCode())) {
        errorList.add(
            String.format(
                I18n.get(
                    AccountExceptionMessage
                        .ACCOUNTING_REPORT_DAS2_DECLARED_PARTNER_MISSING_REGISTRATION_CODE),
                partnerSeq,
                partnerSimpleFullName));
      }
    }
  }
}
