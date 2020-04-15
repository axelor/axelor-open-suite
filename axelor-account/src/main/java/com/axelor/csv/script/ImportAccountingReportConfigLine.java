package com.axelor.csv.script;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.AccountingReportConfigLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.AccountingReportConfigLineRepository;
import com.axelor.exception.AxelorException;
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
  public ImportAccountingReportConfigLine(AccountRepository accountRepo, AccountTypeRepository accountTypeRepo, AccountingReportConfigLineRepository accountingReportConfigLineRepo) {
    this.accountRepo = accountRepo;
    this.accountTypeRepo = accountTypeRepo;
    this.accountingReportConfigLineRepo = accountingReportConfigLineRepo;
  }

  @Transactional(rollbackOn = {Exception.class})
  public Object setAccounts(Object bean, Map<String, Object> values) throws AxelorException {
    assert bean instanceof AccountingReportConfigLine;
    AccountingReportConfigLine configLine = (AccountingReportConfigLine) bean;

    if (configLine.getRuleTypeSelect() == AccountingReportConfigLineRepository.RULE_TYPE_SUM_OF_ACCOUNTS) {
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

  private boolean compareCodes(String code1, String code2) {
    code1 = removeZeros(code1);
    code2 = removeZeros(code2);
    return code1.equals(code2);
  }

  private String removeZeros(String code) {
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
