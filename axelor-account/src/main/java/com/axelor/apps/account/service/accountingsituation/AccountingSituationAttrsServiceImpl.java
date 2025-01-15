/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.accountingsituation;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.common.ObjectUtils;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AccountingSituationAttrsServiceImpl implements AccountingSituationAttrsService {

  protected AppAccountService appAccountService;
  protected AccountConfigService accountConfigService;
  protected PaymentModeService paymentModeService;

  @Inject
  public AccountingSituationAttrsServiceImpl(
      AppAccountService appAccountService,
      AccountConfigService accountConfigService,
      PaymentModeService paymentModeService) {
    this.appAccountService = appAccountService;
    this.accountConfigService = accountConfigService;
    this.paymentModeService = paymentModeService;
  }

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void manageBankDetails(Map<String, Map<String, Object>> attrsMap) {
    boolean manageMultiBanks = appAccountService.getAppBase().getManageMultiBanks();

    this.addAttr("companyInBankDetails", "hidden", !manageMultiBanks, attrsMap);
    this.addAttr("companyOutBankDetails", "hidden", !manageMultiBanks, attrsMap);
  }

  @Override
  public void managePfpValidatorUser(
      Company company, Partner partner, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException {
    boolean isPfpUserRequired = false;

    if (partner != null && partner.getIsSupplier() && company != null) {
      isPfpUserRequired =
          appAccountService.getAppAccount().getActivatePassedForPayment()
              && accountConfigService.getAccountConfig(company).getIsManagePassedForPayment();
    }

    this.addAttr("pfpValidatorUser", "required", isPfpUserRequired, attrsMap);
    this.addAttr("pfpValidatorUser", "hidden", !isPfpUserRequired, attrsMap);
  }

  @Override
  public void hideAccountsLinkToPartner(
      Partner partner, Map<String, Map<String, Object>> attrsMap) {

    this.addAttr(
        "supplierAccount", "hidden", partner != null && !partner.getIsSupplier(), attrsMap);
    this.addAttr(
        "customerAccount", "hidden", partner != null && !partner.getIsCustomer(), attrsMap);
    this.addAttr(
        "employeeAccount", "hidden", partner != null && !partner.getIsEmployee(), attrsMap);
  }

  @Override
  public void manageAnalyticAccountPanel(
      Company company, Partner partner, Map<String, Map<String, Object>> attrsMap) {

    this.addAttr(
        "analyticsPanel",
        "hidden",
        partner == null
            || company == null
            || company.getAccountConfig() == null
            || company.getAccountConfig().getAnalyticDistributionTypeSelect()
                != AccountConfigRepository.DISTRIBUTION_TYPE_PARTNER,
        attrsMap);
  }

  @Override
  public void addCompanyDomain(
      AccountingSituation accountingSituation,
      Partner partner,
      Map<String, Map<String, Object>> attrsMap) {

    String domain = getCompanyDomain(accountingSituation, partner);

    this.addAttr("company", "domain", domain, attrsMap);
  }

  @Override
  public void addCompanyInBankDetailsDomain(
      AccountingSituation accountingSituation,
      Partner partner,
      boolean isInBankDetails,
      Map<String, Map<String, Object>> attrsMap) {
    String field = isInBankDetails ? "companyInBankDetails" : "companyOutBankDetails";
    String domain = "self.id = 0";

    if (partner != null) {
      PaymentMode paymentMode =
          isInBankDetails ? partner.getInPaymentMode() : partner.getOutPaymentMode();
      domain = createDomainForBankDetails(accountingSituation, paymentMode);
    }

    this.addAttr(field, "domain", domain, attrsMap);
  }

  protected String getCompanyDomain(AccountingSituation accountingSituation, Partner partner) {
    if (accountingSituation == null || partner == null) {
      return "self.id = 0";
    }
    String domain = "(self.archived = false OR self.archived is null)";
    List<AccountingSituation> partnerAccountingSituationList = partner.getAccountingSituationList();
    if (ObjectUtils.isEmpty(partnerAccountingSituationList)) {
      return domain;
    }

    domain =
        domain.concat(
            String.format(
                " AND self.id NOT IN (%s)",
                StringHelper.getIdListString(
                    partnerAccountingSituationList.stream()
                        .map(AccountingSituation::getCompany)
                        .collect(Collectors.toList()))));

    return domain;
  }

  /**
   * Creates the domain for the bank details in Accounting Situation
   *
   * @param accountingSituation
   * @param paymentMode
   * @return the domain of the bank details field
   */
  protected String createDomainForBankDetails(
      AccountingSituation accountingSituation, PaymentMode paymentMode) {
    String domain = "self.id = 0";
    List<BankDetails> authorizedBankDetailsList;
    if (paymentMode != null) {
      authorizedBankDetailsList =
          paymentModeService.getCompatibleBankDetailsList(
              paymentMode, accountingSituation.getCompany());
      if (!ObjectUtils.isEmpty(authorizedBankDetailsList)) {
        domain =
            String.format(
                "self.id IN (%s) AND self.active = true",
                StringHelper.getIdListString(authorizedBankDetailsList));
      }
    }
    return domain;
  }
}
