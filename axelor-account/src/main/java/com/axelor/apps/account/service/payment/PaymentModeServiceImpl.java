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
package com.axelor.apps.account.service.payment;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class PaymentModeServiceImpl implements PaymentModeService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppAccountService appAccountService;
  protected AccountManagementAccountService accountManagementAccountService;

  @Inject
  public PaymentModeServiceImpl(
      AppAccountService appAccountService,
      AccountManagementAccountService accountManagementAccountService) {
    this.appAccountService = appAccountService;
    this.accountManagementAccountService = accountManagementAccountService;
  }

  /**
   * Get cash account from PaymentMode, Company, BankDetails.
   *
   * @return
   */
  @Override
  public Account getPaymentModeAccount(Move move) throws AxelorException {
    PaymentMode paymentMode = move.getPaymentMode();
    Company company = move.getCompany();
    Journal journal = move.getJournal();
    log.debug(
        "Fetching account from payment mode {} associated to the company {} with journal {}",
        paymentMode.getName(),
        company.getName(),
        journal.getName());
    if (paymentMode.getAccountManagementList() == null) {
      return null;
    }
    BankDetails defaultBankDetails = company.getDefaultBankDetails();
    Optional<Account> accountOpt =
        getAccountInAccountManagementList(
            paymentMode.getAccountManagementList(), company, journal, defaultBankDetails);

    if (!accountOpt.isPresent()) {
      String exceptionMessage =
          I18n.get(AccountExceptionMessage.PAYMENT_MODE_ERROR_GETTING_ACCOUNT_FROM_PAYMENT_MODE);
      exceptionMessage += " ";
      exceptionMessage +=
          I18n.get("Company")
              + " : %s, "
              + I18n.get("Payment mode")
              + " : %s, "
              + I18n.get("Journal")
              + " : %s, ";
      if (defaultBankDetails != null) {
        exceptionMessage += I18n.get("Bank details") + " : %s, ";
      }

      exceptionMessage += I18n.get(AccountExceptionMessage.PAYMENT_MODE_1);
      exceptionMessage =
          String.format(
              exceptionMessage,
              company.getName(),
              paymentMode.getName(),
              journal.getName(),
              defaultBankDetails != null ? defaultBankDetails.getFullName() : null);
      throw new AxelorException(
          paymentMode, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, exceptionMessage);
    } else {
      return accountOpt.get();
    }
  }

  protected Optional<Account> getAccountInAccountManagementList(
      List<AccountManagement> accountManagementList,
      Company company,
      Journal journal,
      BankDetails defaultBankDetails) {
    List<AccountManagement> accountManagementFiltered =
        accountManagementList.stream()
            .filter(
                accountManagement ->
                    company.equals(accountManagement.getCompany())
                        && journal.equals(accountManagement.getJournal()))
            .sorted(Comparator.comparing(AccountManagement::getId))
            .collect(Collectors.toList());
    Optional<Account> accountOpt = Optional.empty();
    if (defaultBankDetails != null) {
      accountOpt =
          accountManagementFiltered.stream()
              .filter(
                  accountManagement ->
                      defaultBankDetails.equals(accountManagement.getBankDetails()))
              .map(AccountManagement::getCashAccount)
              .findFirst();
    }
    if (!accountOpt.isPresent()) {
      accountOpt =
          accountManagementFiltered.stream().map(AccountManagement::getCashAccount).findFirst();
    }
    return accountOpt;
  }

  @Override
  public Account getPaymentModeAccount(
      PaymentMode paymentMode, Company company, BankDetails bankDetails) throws AxelorException {
    return getPaymentModeAccount(paymentMode, company, bankDetails, false);
  }

  /**
   * Get cash or global accrount from PaymenMode, Company, BankDetails function of boolean global.
   *
   * @return
   */
  @Override
  public Account getPaymentModeAccount(
      PaymentMode paymentMode, Company company, BankDetails bankDetails, boolean global)
      throws AxelorException {

    log.debug(
        "Fetching account from payment mode {} associated to the company {}",
        paymentMode.getName(),
        company.getName());

    AccountManagement accountManagement =
        this.getAccountManagement(paymentMode, company, bankDetails);

    if (Objects.nonNull(accountManagement)) {
      if (!global && Objects.nonNull(accountManagement.getCashAccount())) {
        return accountManagement.getCashAccount();
      }
      if (global && Objects.nonNull(accountManagement.getGlobalAccountingCashAccount())) {
        return accountManagement.getGlobalAccountingCashAccount();
      }
    }

    throw new AxelorException(
        paymentMode,
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(AccountExceptionMessage.PAYMENT_MODE_CASH_ACCOUNT),
        global ? String.format("%s ", I18n.get("global")) : "",
        company.getName(),
        paymentMode.getName());
  }

  @Override
  public AccountManagement getAccountManagement(
      PaymentMode paymentMode, Company company, BankDetails bankDetails) {

    if (paymentMode.getAccountManagementList() == null) {
      return null;
    }

    if (!appAccountService.getAppBase().getManageMultiBanks()) {
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

    if (accountManagement == null && appAccountService.getAppBase().getManageMultiBanks()) {
      throw new AxelorException(
          paymentMode,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.PAYMENT_MODE_3),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          company.getName(),
          paymentMode.getName());
    } else if (accountManagement == null || accountManagement.getSequence() == null) {
      throw new AxelorException(
          paymentMode,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.PAYMENT_MODE_1),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          company.getName(),
          paymentMode.getName());
    }

    return accountManagement.getSequence();
  }

  /**
   * Get journal from PaymentMode, Company, BankDetails
   *
   * @return
   */
  @Override
  public Journal getPaymentModeJournal(
      PaymentMode paymentMode, Company company, BankDetails bankDetails) throws AxelorException {
    return getPaymentModeJournal(paymentMode, company, bankDetails, false);
  }

  /**
   * Get journal or cheque deposit journal from PaymenMode, Company, BankDetails function of boolean
   * global.
   *
   * @return
   */
  @Override
  public Journal getPaymentModeJournal(
      PaymentMode paymentMode, Company company, BankDetails bankDetails, boolean global)
      throws AxelorException {

    AccountManagement accountManagement =
        this.getAccountManagement(paymentMode, company, bankDetails);

    if (Objects.isNull(accountManagement) && appAccountService.getAppBase().getManageMultiBanks()) {
      throw new AxelorException(
          paymentMode,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.PAYMENT_MODE_3),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          company.getName(),
          paymentMode.getName());
    }

    if (Objects.nonNull(accountManagement)) {
      if (!global && Objects.nonNull(accountManagement.getJournal())) {
        return accountManagement.getJournal();
      }
      if (global && Objects.nonNull(accountManagement.getChequeDepositJournal())) {
        return accountManagement.getChequeDepositJournal();
      }
    }

    throw new AxelorException(
        paymentMode,
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(AccountExceptionMessage.PAYMENT_MODE_2),
        I18n.get(BaseExceptionMessage.EXCEPTION),
        company.getName(),
        paymentMode.getName());
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

  @Override
  public boolean isPendingPayment(PaymentMode paymentMode) {
    if (paymentMode == null) {
      return false;
    }

    int typeSelect = paymentMode.getTypeSelect();
    int inOutSelect = paymentMode.getInOutSelect();

    return (typeSelect == PaymentModeRepository.TYPE_DD && inOutSelect == PaymentModeRepository.IN)
        || (typeSelect == PaymentModeRepository.TYPE_TRANSFER
            && inOutSelect == PaymentModeRepository.OUT)
        || (typeSelect == PaymentModeRepository.TYPE_EXCHANGES
            && inOutSelect == PaymentModeRepository.IN);
  }
}
