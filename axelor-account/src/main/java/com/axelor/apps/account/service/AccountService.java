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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.AnalyticRulesRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.StringHelper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /** Debit balance = debit - credit */
  public static final Integer BALANCE_TYPE_DEBIT_BALANCE = 1;

  /** Credit balance = credit - debit */
  public static final Integer BALANCE_TYPE_CREDIT_BALANCE = 2;

  public static final int MAX_LEVEL_OF_ACCOUNT = 20;

  protected AccountRepository accountRepository;
  protected AccountConfigService accountConfigService;
  protected AnalyticRulesRepository analyticRulesRepository;

  @Inject
  public AccountService(
      AccountRepository accountRepository,
      AccountConfigService accountConfigService,
      AnalyticRulesRepository analyticRulesRepository) {
    this.accountRepository = accountRepository;
    this.accountConfigService = accountConfigService;
    this.analyticRulesRepository = analyticRulesRepository;
  }

  /**
   * Compute the balance of the account, depending of the balance type
   *
   * @param account Account
   * @param balanceType
   *     <p>1 : debit balance = debit - credit
   *     <p>2 : credit balance = credit - debit
   * @return The balance (debit balance or credit balance)
   */
  public BigDecimal computeBalance(Account account, int balanceType) {
    return this.computeBalance(account, null, null, balanceType);
  }

  public BigDecimal computeBalance(List<AccountType> accountTypeList, Year year, int balanceType) {
    return this.computeBalance(null, accountTypeList, year, balanceType);
  }

  protected BigDecimal computeBalance(
      Account account, List<AccountType> accountTypeList, Year year, int balanceType) {
    Query balanceQuery =
        JPA.em()
            .createQuery(
                String.format(
                    "select sum(self.debit - self.credit) from MoveLine self where self.account%s IN (:account) "
                        + "and self.move.ignoreInAccountingOk IN ('false', null) and self.move.statusSelect IN ("
                        + Joiner.on(',')
                            .join(
                                Lists.newArrayList(
                                    MoveRepository.STATUS_ACCOUNTED, MoveRepository.STATUS_DAYBOOK))
                        + ") %s",
                    account == null ? ".accountType" : "",
                    year != null ? " and self.move.period.year = :year" : ""));

    balanceQuery.setParameter("account", account != null ? account : accountTypeList);

    if (year != null) {
      balanceQuery.setParameter("year", year);
    }

    BigDecimal balance = (BigDecimal) balanceQuery.getSingleResult();

    if (balance != null) {

      if (balanceType == BALANCE_TYPE_CREDIT_BALANCE) {
        balance = balance.negate();
      }
      log.debug("Account balance : {}", balance);

      return balance;
    } else {
      return BigDecimal.ZERO;
    }
  }

  public List<Long> getAllAccountsSubAccountIncluded(List<Long> accountList) {

    return getAllAccountsSubAccountIncluded(accountList, 0);
  }

  public List<Long> getAllAccountsSubAccountIncluded(List<Long> accountList, int counter) {

    if (counter > MAX_LEVEL_OF_ACCOUNT) {
      return new ArrayList<>();
    }
    counter++;

    List<Long> allAccountsSubAccountIncluded = new ArrayList<>();
    if (accountList != null && !accountList.isEmpty()) {
      allAccountsSubAccountIncluded.addAll(accountList);

      for (Long accountId : accountList) {

        allAccountsSubAccountIncluded.addAll(
            getAllAccountsSubAccountIncluded(getSubAccounts(accountId), counter));
      }
    }
    return allAccountsSubAccountIncluded;
  }

  public List<Long> getSubAccounts(Long accountId) {

    return accountRepository
        .all()
        .filter("self.parentAccount.id = ?1", accountId)
        .select("id")
        .fetch(0, 0)
        .stream()
        .map(m -> (Long) m.get("id"))
        .collect(Collectors.toList());
  }

  @Transactional
  public void toggleStatusSelect(Account account) {
    if (account != null) {
      if (account.getStatusSelect() == AccountRepository.STATUS_INACTIVE) {
        account = activate(account);
      } else {
        account = desactivate(account);
      }
      accountRepository.save(account);
    }
  }

  public Account fillAccountCode(Account account) throws AxelorException {
    String code = account.getCode();
    if (StringUtils.notEmpty(code) && account.getCompany() != null) {
      int accountCodeNbrCharSelect =
          accountConfigService.getAccountConfig(account.getCompany()).getAccountCodeNbrCharSelect();
      int accountCodeLength = code.length();
      if (accountCodeLength > accountCodeNbrCharSelect) {
        account.setCode(code.substring(0, accountCodeNbrCharSelect));
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.ACCOUNT_CODE_CHAR_EXCEEDED),
            accountCodeLength,
            accountCodeNbrCharSelect);
      } else if (accountCodeLength < accountCodeNbrCharSelect
          && !account.getIsRegulatoryAccount()
          && account.getAccountType() != null
          && !AccountTypeRepository.TYPE_VIEW.equals(
              account.getAccountType().getTechnicalTypeSelect())) {
        account.setCode(StringHelper.fillStringRight(code, '0', accountCodeNbrCharSelect));
      }
    }
    return account;
  }

  public Account fillAccountCodeOnImport(Account account, int lineNo) throws AxelorException {
    String code = account.getCode();
    if (StringUtils.notEmpty(code) && account.getCompany() != null) {
      int accountCodeNbrCharSelect =
          accountConfigService.getAccountConfig(account.getCompany()).getAccountCodeNbrCharSelect();
      int accountCodeLength = code.length();
      if (accountCodeLength > accountCodeNbrCharSelect) {
        account.setCode(code.substring(0, accountCodeNbrCharSelect));
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(AccountExceptionMessage.ACCOUNT_CODE_CHAR_EXCEEDED_IMPORT),
            lineNo,
            account.getCode());
      } else if (accountCodeLength < accountCodeNbrCharSelect
          && !account.getIsRegulatoryAccount()
          && account.getAccountType() != null
          && !AccountTypeRepository.TYPE_VIEW.equals(
              account.getAccountType().getTechnicalTypeSelect())) {
        account.setCode(StringHelper.fillStringRight(code, '0', accountCodeNbrCharSelect));
      }
    }
    return account;
  }

  public List<Long> getAnalyticAccountsIds(Account account) {
    Query query =
        JPA.em()
            .createQuery(
                "SELECT DISTINCT analyticAccount.id FROM AnalyticRules analyticRules "
                    + "JOIN analyticRules.analyticAccountSet analyticAccount "
                    + "WHERE analyticRules.fromAccount.code <= :account "
                    + "AND analyticRules.toAccount.code >= :account "
                    + "AND analyticRules.company = :company");
    query.setParameter("account", account.getCode());
    query.setParameter("company", account.getCompany());
    return query.getResultList();
  }

  protected Account activate(Account account) {
    account.setStatusSelect(AccountRepository.STATUS_ACTIVE);
    return account;
  }

  protected Account desactivate(Account account) {
    account.setStatusSelect(AccountRepository.STATUS_INACTIVE);
    return account;
  }
}
