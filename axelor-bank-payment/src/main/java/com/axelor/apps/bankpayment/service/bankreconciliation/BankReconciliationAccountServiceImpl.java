package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.repo.AccountManagementRepository;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.base.db.BankDetails;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BankReconciliationAccountServiceImpl implements BankReconciliationAccountService {

  protected JournalRepository journalRepository;
  protected AccountRepository accountRepository;
  protected AccountManagementRepository accountManagementRepository;

  @Inject
  public BankReconciliationAccountServiceImpl(
      JournalRepository journalRepository,
      AccountRepository accountRepository,
      AccountManagementRepository accountManagementRepository) {
    this.journalRepository = journalRepository;
    this.accountRepository = accountRepository;
    this.accountManagementRepository = accountManagementRepository;
  }

  @Override
  public Journal getJournal(BankReconciliation bankReconciliation) {

    Journal journal = null;
    String journalIds = String.join(",", getAccountManagementJournals(bankReconciliation));
    if (bankReconciliation.getBankDetails().getJournal() != null) {
      journal = bankReconciliation.getBankDetails().getJournal();
    } else if (!Strings.isNullOrEmpty(journalIds) && (journalIds.split(",").length) == 1) {
      journal = journalRepository.find(Long.parseLong(journalIds));
    }
    return journal;
  }

  @Override
  public Account getCashAccount(BankReconciliation bankReconciliation) {

    Account cashAccount = null;
    String cashAccountIds = String.join(",", getAccountManagementCashAccounts(bankReconciliation));
    if (bankReconciliation.getBankDetails().getBankAccount() != null) {
      cashAccount = bankReconciliation.getBankDetails().getBankAccount();

    } else if (!Strings.isNullOrEmpty(cashAccountIds) && (cashAccountIds.split(",").length) == 1) {
      cashAccount = accountRepository.find(Long.parseLong(cashAccountIds));
    }
    return cashAccount;
  }

  @Override
  public Set<String> getAccountManagementJournals(BankReconciliation bankReconciliation) {
    Set<String> journalIdSet = new HashSet<String>();
    Account cashAccount = bankReconciliation.getCashAccount();
    List<AccountManagement> accountManagementList = new ArrayList<>();

    if (cashAccount != null) {
      accountManagementList =
          accountManagementRepository
              .all()
              .filter(
                  "self.bankDetails = ?1 and self.cashAccount = ?2",
                  bankReconciliation.getBankDetails(),
                  cashAccount)
              .fetch();
    } else {
      accountManagementList =
          accountManagementRepository
              .all()
              .filter("self.bankDetails = ?1", bankReconciliation.getBankDetails())
              .fetch();
    }

    for (AccountManagement accountManagement : accountManagementList) {
      if (accountManagement.getJournal() != null) {
        journalIdSet.add(accountManagement.getJournal().getId().toString());
      }
    }
    return journalIdSet;
  }

  @Override
  public Set<String> getAccountManagementCashAccounts(BankReconciliation bankReconciliation) {
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
      if (accountManagement.getCashAccount() != null
          && accountManagement.getCashAccount().getAccountType() != null
          && AccountTypeRepository.TYPE_CASH.equals(
              accountManagement.getCashAccount().getAccountType().getTechnicalTypeSelect())) {
        cashAccountIdSet.add(accountManagement.getCashAccount().getId().toString());
      }
    }
    return cashAccountIdSet;
  }
}
