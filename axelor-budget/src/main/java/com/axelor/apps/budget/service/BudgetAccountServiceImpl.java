package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import java.util.Arrays;
import java.util.List;

public class BudgetAccountServiceImpl implements BudgetAccountService {
  @Override
  public boolean checkAccountType(Account account) {
    if (account == null || account.getAccountType() == null) {
      return false;
    }

    List<String> accountTypeList =
        Arrays.asList(
            AccountTypeRepository.TYPE_CHARGE,
            AccountTypeRepository.TYPE_INCOME,
            AccountTypeRepository.TYPE_IMMOBILISATION);

    return accountTypeList.contains(account.getAccountType().getTechnicalTypeSelect());
  }
}
