/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.repo.AccountManagementRepository;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.service.AccountService;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementFileFormat;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementFileFormatRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.load.BankReconciliationLoadService;
import com.axelor.apps.bankpayment.service.bankreconciliation.load.afb120.BankReconciliationLoadAFB120Service;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BankReconciliationService {

  protected BankReconciliationRepository bankReconciliationRepository;
  protected AccountService accountService;
  protected AccountManagementRepository accountManagementRepository;

  @Inject
  public BankReconciliationService(
      BankReconciliationRepository bankReconciliationRepository,
      AccountService accountService,
      AccountManagementRepository accountManagementRepository) {

    this.bankReconciliationRepository = bankReconciliationRepository;
    this.accountService = accountService;
    this.accountManagementRepository = accountManagementRepository;
  }

  @Transactional
  public void compute(BankReconciliation bankReconciliation) {

    BigDecimal totalPaid = BigDecimal.ZERO;
    BigDecimal totalCashed = BigDecimal.ZERO;

    for (BankReconciliationLine bankReconciliationLine :
        bankReconciliation.getBankReconciliationLineList()) {
      totalPaid = totalPaid.add(bankReconciliationLine.getDebit());
      totalCashed = totalCashed.add(bankReconciliationLine.getCredit());
    }

    bankReconciliation.setTotalPaid(totalPaid);
    bankReconciliation.setTotalCashed(totalCashed);
    Account cashAccount = bankReconciliation.getCashAccount();
    if (cashAccount != null) {
      bankReconciliation.setAccountBalance(
          accountService.computeBalance(cashAccount, AccountService.BALANCE_TYPE_DEBIT_BALANCE));
    }
    bankReconciliation.setComputedBalance(
        bankReconciliation.getAccountBalance().add(totalCashed).subtract(totalPaid));
  }

  public String createDomainForBankDetails(BankReconciliation bankReconciliation) {

    return Beans.get(BankDetailsService.class)
        .getActiveCompanyBankDetails(
            bankReconciliation.getCompany(), bankReconciliation.getCurrency());
  }

  @Transactional
  public void loadBankStatement(BankReconciliation bankReconciliation) {
    loadBankStatement(bankReconciliation, true);
  }

  @Transactional
  public void loadBankStatement(
      BankReconciliation bankReconciliation, boolean includeBankStatement) {

    BankStatement bankStatement = bankReconciliation.getBankStatement();

    BankStatementFileFormat bankStatementFileFormat = bankStatement.getBankStatementFileFormat();

    switch (bankStatementFileFormat.getStatementFileFormatSelect()) {
      case BankStatementFileFormatRepository.FILE_FORMAT_CAMT_XXX_CFONB120_REP:
      case BankStatementFileFormatRepository.FILE_FORMAT_CAMT_XXX_CFONB120_STM:
        Beans.get(BankReconciliationLoadAFB120Service.class)
            .loadBankStatement(bankReconciliation, includeBankStatement);
        break;

      default:
        Beans.get(BankReconciliationLoadService.class)
            .loadBankStatement(bankReconciliation, includeBankStatement);
    }

    compute(bankReconciliation);

    bankReconciliationRepository.save(bankReconciliation);
  }

  public String getJournalDomain(BankReconciliation bankReconciliation) {

    String journalIds = null;
    Set<String> journalIdSet = new HashSet<String>();

    List<AccountManagement> accountManagementList =
        accountManagementRepository
            .all()
            .filter("self.bankDetails = ?1", bankReconciliation.getBankDetails())
            .fetch();

    for (AccountManagement accountManagement : accountManagementList) {
      if (accountManagement.getJournal() != null) {
        journalIdSet.add(accountManagement.getJournal().getId().toString());
      }
    }

    if (journalIdSet.isEmpty()) {
      if (bankReconciliation.getBankDetails().getJournal() != null) {
        return bankReconciliation.getBankDetails().getJournal().getId().toString();
      }
    }

    journalIds = String.join(",", journalIdSet);

    return journalIds;
  }

  public Journal getJournal(BankReconciliation bankReconciliation) {

    Journal journal = null;
    String journalIds = getJournalDomain(bankReconciliation);
    if (Strings.isNullOrEmpty(journalIds)) {
      if (bankReconciliation.getBankDetails().getJournal() != null) {
        journal = bankReconciliation.getBankDetails().getJournal();
      }
    } else if ((journalIds.split(",").length) == 1) {
      journal = Beans.get(JournalRepository.class).find(Long.parseLong(journalIds));
    }

    return journal;
  }

  public String getCashAccountDomain(BankReconciliation bankReconciliation) {

    String cashAccountIds = null;
    List<AccountManagement> accountManagementList;
    Journal journal = bankReconciliation.getJournal();
    Set<String> cashAccountIdSet = new HashSet<String>();
    BankDetails bankDetails = bankReconciliation.getBankDetails();

    if (journal != null) {
      accountManagementList =
          accountManagementRepository
              .all()
              .filter("self.bankDetails = ?1 AND self.journal = ?2", bankDetails, journal)
              .fetch();
    } else {
      accountManagementList =
          accountManagementRepository.all().filter("self.bankDetails = ?1", bankDetails).fetch();
    }

    for (AccountManagement accountManagement : accountManagementList) {
      if (accountManagement.getCashAccount() != null) {
        cashAccountIdSet.add(accountManagement.getCashAccount().getId().toString());
      }
    }

    if (cashAccountIdSet.isEmpty()) {
      if (bankReconciliation.getBankDetails().getBankAccount() != null) {
        return bankReconciliation.getBankDetails().getBankAccount().getId().toString();
      }
    }

    cashAccountIds = String.join(",", cashAccountIdSet);

    return cashAccountIds;
  }

  public Account getCashAccount(BankReconciliation bankReconciliation) {

    Account cashAccount = null;
    String cashAccountIds = getCashAccountDomain(bankReconciliation);
    if (Strings.isNullOrEmpty(cashAccountIds)) {
      if (bankReconciliation.getBankDetails().getBankAccount() != null) {
        cashAccount = bankReconciliation.getBankDetails().getBankAccount();
      }
    } else if ((cashAccountIds.split(",").length) == 1) {
      cashAccount = Beans.get(AccountRepository.class).find(Long.parseLong(cashAccountIds));
    }
    return cashAccount;
  }
}
