package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.Account;

public interface BudgetAccountService {
  boolean checkAccountType(Account account);
}
