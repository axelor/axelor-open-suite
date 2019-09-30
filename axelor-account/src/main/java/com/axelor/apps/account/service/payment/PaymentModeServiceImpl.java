/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.payment;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentModeServiceImpl implements PaymentModeService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public Account getPaymentModeAccount(
      PaymentMode paymentMode, Company company, BankDetails bankDetails) throws AxelorException {

    log.debug(
        "Récupération du compte comptable du mode de paiement associé à la société :"
            + " Société : {}, Mode de paiement : {}",
        new Object[] {company.getName(), paymentMode.getName()});

    AccountManagement accountManagement =
        this.getAccountManagement(paymentMode, company, bankDetails);

    if (accountManagement != null && accountManagement.getCashAccount() != null) {
      return accountManagement.getCashAccount();
    }

    throw new AxelorException(
        paymentMode,
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get("Company")
            + " : %s, "
            + I18n.get("Payment mode")
            + " : %s: "
            + I18n.get(IExceptionMessage.PAYMENT_MODE_1),
        company.getName(),
        paymentMode.getName());
  }

  @Override
  public AccountManagement getAccountManagement(
      PaymentMode paymentMode, Company company, BankDetails bankDetails) {

    if (paymentMode.getAccountManagementList() == null) {
      return null;
    }

    if (!Beans.get(AppAccountService.class).getAppBase().getManageMultiBanks()) {
      return getAccountManagement(paymentMode, company);
    }

    for (AccountManagement accountManagement : paymentMode.getAccountManagementList()) {

      if (accountManagement.getCompany() != null
          && accountManagement.getCompany().equals(company)
          && accountManagement.getBankDetails() != null
          && accountManagement.getBankDetails().equals(bankDetails)) {

        return accountManagement;
      }
    }

    return null;
  }

  protected AccountManagement getAccountManagement(PaymentMode paymentMode, Company company) {

    if (paymentMode.getAccountManagementList() == null) {
      return null;
    }

    return Beans.get(AccountManagementService.class)
        .getAccountManagement(paymentMode.getAccountManagementList(), company);
  }

  @Override
  public Sequence getPaymentModeSequence(
      PaymentMode paymentMode, Company company, BankDetails bankDetails) throws AxelorException {

    AccountManagement accountManagement =
        this.getAccountManagement(paymentMode, company, bankDetails);

    if (accountManagement == null || accountManagement.getSequence() == null) {
      throw new AxelorException(
          paymentMode,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PAYMENT_MODE_2),
          I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION),
          company.getName(),
          paymentMode.getName());
    }

    return accountManagement.getSequence();
  }

  @Override
  public Journal getPaymentModeJournal(
      PaymentMode paymentMode, Company company, BankDetails bankDetails) throws AxelorException {

    AccountManagement accountManagement =
        this.getAccountManagement(paymentMode, company, bankDetails);

    if (accountManagement == null || accountManagement.getJournal() == null) {
      throw new AxelorException(
          paymentMode,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.PAYMENT_MODE_3),
          I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION),
          company.getName(),
          paymentMode.getName());
    }

    return accountManagement.getJournal();
  }

  @Override
  public List<BankDetails> getCompatibleBankDetailsList(PaymentMode paymentMode, Company company) {
    List<BankDetails> bankDetailsList = new ArrayList<>();
    if (paymentMode == null) {
      return bankDetailsList;
    }
    List<AccountManagement> accountManagementList = paymentMode.getAccountManagementList();
    if (accountManagementList == null) {
      return bankDetailsList;
    }
    for (AccountManagement accountManagement : accountManagementList) {
      if (accountManagement.getCompany().equals(company)
          && accountManagement.getBankDetails() != null
          && accountManagement.getBankDetails().getActive()) {
        bankDetailsList.add(accountManagement.getBankDetails());
      }
    }
    return bankDetailsList;
  }

  @Override
  public PaymentMode reverseInOut(PaymentMode paymentMode) {
    if (paymentMode == null) {
      return null;
    }
    int inversedInOrOut =
        paymentMode.getInOutSelect() == PaymentModeRepository.IN
            ? PaymentModeRepository.OUT
            : PaymentModeRepository.IN;

    return Beans.get(PaymentModeRepository.class)
        .all()
        .filter("self.typeSelect = :_paymentModeType AND self.inOutSelect = :_inversedInOrOut")
        .bind("_paymentModeType", paymentMode.getTypeSelect())
        .bind("_inversedInOrOut", inversedInOrOut)
        .fetchOne();
  }
}
