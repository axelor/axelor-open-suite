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
package com.axelor.csv.script;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.AccountingReportConfigLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.AccountingReportConfigLineRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImportAccountingReportConfigLine {
  private AccountRepository accountRepo;
  private AccountTypeRepository accountTypeRepo;
  private AccountingReportConfigLineRepository accountingReportConfigLineRepo;

  @Inject
  public ImportAccountingReportConfigLine(
      AccountRepository accountRepo,
      AccountTypeRepository accountTypeRepo,
      AccountingReportConfigLineRepository accountingReportConfigLineRepo) {
    this.accountRepo = accountRepo;
    this.accountTypeRepo = accountTypeRepo;
    this.accountingReportConfigLineRepo = accountingReportConfigLineRepo;
  }

  @Transactional
  public Object setAccounts(Object bean, Map<String, Object> values) {
    assert bean instanceof AccountingReportConfigLine;
    AccountingReportConfigLine configLine = (AccountingReportConfigLine) bean;

    if (configLine.getRuleTypeSelect()
        == AccountingReportConfigLineRepository.RULE_TYPE_SUM_OF_ACCOUNTS) {
      String accountTypeValues = (String) values.get("accountType");
      if (accountTypeValues != null && !accountTypeValues.isEmpty()) {
        String[] types = accountTypeValues.split("\\|");
        Set<AccountType> accountTypes = new HashSet<>();
        AccountType typeToAdd;
        for (String type : types) {
          typeToAdd =
              accountTypeRepo
                  .all()
                  .filter("self.importId = :importId")
                  .bind("importId", type)
                  .fetchOne();
          if (typeToAdd != null) {
            accountTypes.add(typeToAdd);
          }
        }
        configLine.setAccountTypeSet(accountTypes);
      }

      String accountValues = (String) values.get("accountCode");
      if (accountValues != null && !accountValues.isEmpty()) {
        String[] codes = accountValues.split("\\|");
        Set<Account> accounts = new HashSet<>();
        Account accountToAdd;
        List<Account> fetched;
        for (String code : codes) {
          accountToAdd =
              accountRepo.all().filter("self.code = :code").bind("code", code).fetchOne();

          if (accountToAdd == null) {
            fetched = accountRepo.all().fetch();
            for (Account account : fetched) {
              if (compareCodes(account.getCode(), code)) {
                accountToAdd = account;
                break;
              }
            }
          }

          if (accountToAdd != null) {
            accounts.add(accountToAdd);
          }
        }
        configLine.setAccountSet(accounts);
      }

      accountingReportConfigLineRepo.save(configLine);
    }

    return configLine;
  }

  protected boolean compareCodes(String code1, String code2) {
    code1 = removeZeros(code1);
    code2 = removeZeros(code2);
    return code1.equals(code2);
  }

  protected String removeZeros(String code) {
    StringBuffer sb = new StringBuffer(code);
    sb.reverse();
    code = sb.toString();

    int i = 0;
    while (i < code.length() && code.charAt(i) == '0') i++;

    sb = new StringBuffer(code);
    sb.replace(0, i, "");

    return sb.reverse().toString();
  }
}
