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

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.BankDetailsServiceImpl;
import com.axelor.inject.Beans;
import com.axelor.utils.StringTool;
import java.util.ArrayList;
import java.util.List;

public class BankDetailsServiceAccountImpl extends BankDetailsServiceImpl {

  /**
   * In this implementation, we use the O2M in payment mode.
   *
   * @param company
   * @param paymentMode
   * @return
   * @throws AxelorException
   */
  @Override
  public String createCompanyBankDetailsDomain(
      Partner partner, Company company, PaymentMode paymentMode, Integer operationTypeSelect)
      throws AxelorException {

    AppAccountService appAccountService = Beans.get(AppAccountService.class);

    if (!appAccountService.isApp("account")
        || !appAccountService.getAppBase().getManageMultiBanks()) {
      return super.createCompanyBankDetailsDomain(
          partner, company, paymentMode, operationTypeSelect);
    } else if (Boolean.TRUE.equals(appAccountService.getAppAccount().getManageFactors())
        && partner != null
        && Boolean.TRUE.equals(partner.getFactorizedCustomer())) {
      return "self.partner.isFactor = true AND self.active = true";
    } else {
      List<BankDetails> authorizedBankDetails;

      if (paymentMode == null) {
        return "self.id IN (0)";
      }
      List<AccountManagement> accountManagementList = paymentMode.getAccountManagementList();

      authorizedBankDetails = new ArrayList<>();

      for (AccountManagement accountManagement : accountManagementList) {
        if (accountManagement.getCompany() != null
            && accountManagement.getCompany().equals(company)) {
          authorizedBankDetails.add(accountManagement.getBankDetails());
        }
      }

      if (authorizedBankDetails.isEmpty()) {
        return "self.id IN (0)";
      } else {
        return "self.id IN ("
            + StringTool.getIdListString(authorizedBankDetails)
            + ") AND self.active = true";
      }
    }
  }

  /**
   * Find a default bank details.
   *
   * @param company
   * @param paymentMode
   * @param partner
   * @return the default bank details in accounting situation if it is active and allowed by the
   *     payment mode, or an authorized bank details if he is the only one authorized.
   * @throws AxelorException
   */
  @Override
  public BankDetails getDefaultCompanyBankDetails(
      Company company, PaymentMode paymentMode, Partner partner, Integer operationTypeSelect)
      throws AxelorException {

    AppAccountService appAccountService = Beans.get(AppAccountService.class);

    if (!appAccountService.isApp("account")
        || !appAccountService.getAppBase().getManageMultiBanks()) {
      return super.getDefaultCompanyBankDetails(company, paymentMode, partner, operationTypeSelect);
    } else {
      if (paymentMode == null) {
        return null;
      }

      BankDetails candidateBankDetails =
          getDefaultCompanyBankDetailsFromPartner(company, paymentMode, partner);

      List<BankDetails> authorizedBankDetails =
          Beans.get(PaymentModeService.class).getCompatibleBankDetailsList(paymentMode, company);

      if ((partner == null || !partner.getFactorizedCustomer())
          && candidateBankDetails != null
          && authorizedBankDetails.contains(candidateBankDetails)
          && candidateBankDetails.getActive()) {
        return candidateBankDetails;
      }
      // we did not find a bank details in accounting situation
      else {
        if (authorizedBankDetails.size() == 1 && authorizedBankDetails.get(0).getActive()) {
          return authorizedBankDetails.get(0);
        }
      }

      return null;
    }
  }

  /**
   * Looks for the bank details in accounting situation.
   *
   * @param company
   * @param paymentMode
   * @param partner
   * @return The bank details corresponding to the partner and the company with the right payment
   *     mode null if the partner is null or the accounting situation empty
   */
  protected BankDetails getDefaultCompanyBankDetailsFromPartner(
      Company company, PaymentMode paymentMode, Partner partner) {

    if (partner == null) {
      return null;
    }
    AccountingSituation accountingSituation =
        Beans.get(AccountingSituationService.class).getAccountingSituation(partner, company);
    if (accountingSituation == null) {
      return null;
    }

    BankDetails candidateBankDetails = null;
    if (paymentMode.getInOutSelect() == PaymentModeRepository.IN) {
      candidateBankDetails = accountingSituation.getCompanyInBankDetails();
    } else if (paymentMode.getInOutSelect() == PaymentModeRepository.OUT) {
      candidateBankDetails = accountingSituation.getCompanyOutBankDetails();
    }
    return candidateBankDetails;
  }
}
