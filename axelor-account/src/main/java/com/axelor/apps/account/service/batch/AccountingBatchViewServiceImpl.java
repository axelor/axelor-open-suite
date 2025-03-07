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
package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class AccountingBatchViewServiceImpl implements AccountingBatchViewService {

  protected AccountRepository accountRepository;

  @Inject
  public AccountingBatchViewServiceImpl(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Override
  public List<Account> getClosureAccountSet(
      AccountingBatch accountingBatch, boolean closeAllAccounts) {
    return getAccountSet(accountingBatch, closeAllAccounts, true);
  }

  @Override
  public List<Account> getOpeningAccountSet(
      AccountingBatch accountingBatch, boolean openAllAccounts) {
    return getAccountSet(accountingBatch, openAllAccounts, false);
  }

  protected List<Account> getAccountSet(
      AccountingBatch accountingBatch, boolean includeAllAccounts, boolean isClosure) {
    if (!includeAllAccounts) {
      return new ArrayList<>();
    }
    List<String> accountTypeList =
        new ArrayList<>(
            List.of(
                AccountTypeRepository.TYPE_EQUITY,
                AccountTypeRepository.TYPE_PROVISION,
                AccountTypeRepository.TYPE_DEBT,
                AccountTypeRepository.TYPE_IMMOBILISATION,
                AccountTypeRepository.TYPE_CURRENT_ASSET,
                AccountTypeRepository.TYPE_RECEIVABLE,
                AccountTypeRepository.TYPE_PAYABLE,
                AccountTypeRepository.TYPE_TAX,
                AccountTypeRepository.TYPE_CASH,
                AccountTypeRepository.TYPE_ASSET));

    if (isClosure) {
      accountTypeList.add(AccountTypeRepository.TYPE_CHARGE);
      accountTypeList.add(AccountTypeRepository.TYPE_INCOME);
    }

    return accountRepository
        .all()
        .filter(
            "self.company = :company AND self.accountType.technicalTypeSelect IN (:accountTypes)")
        .bind("company", accountingBatch.getCompany())
        .bind("accountTypes", accountTypeList)
        .fetch();
  }
}
