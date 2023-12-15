package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import java.util.Set;

public interface BankReconciliationAccountService {

  Journal getJournal(BankReconciliation bankReconciliation);

  Account getCashAccount(BankReconciliation bankReconciliation);

  Set<String> getAccountManagementCashAccounts(BankReconciliation bankReconciliation);

  Set<String> getAccountManagementJournals(BankReconciliation bankReconciliation);
}
