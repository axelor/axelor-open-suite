/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.repo.AccountAnalyticRulesRepository;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
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
  protected MoveLineRepository moveLineRepository;
  protected AccountAnalyticRulesRepository accountAnalyticRulesRepository;
  protected AccountConfigService accountConfigService;

  @Inject
  public AccountService(
      AccountRepository accountRepository,
      MoveLineRepository moveLineRepository,
      AccountAnalyticRulesRepository accountAnalyticRulesRepository,
      AccountConfigService accountConfigService) {
    this.accountRepository = accountRepository;
    this.moveLineRepository = moveLineRepository;
    this.accountAnalyticRulesRepository = accountAnalyticRulesRepository;
    this.accountConfigService = accountConfigService;
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

    Query balanceQuery =
        JPA.em()
            .createQuery(
                "select sum(self.debit - self.credit) from MoveLine self where self.account = :account "
                    + "and self.move.ignoreInAccountingOk IN ('false', null) and self.move.statusSelect IN (2, 3)");

    balanceQuery.setParameter("account", account);

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

    return accountRepository.all().filter("self.parentAccount.id = ?1", accountId).select("id")
        .fetch(0, 0).stream()
        .map(m -> (Long) m.get("id"))
        .collect(Collectors.toList());
  }

  public void checkAnalyticAxis(Account account) throws AxelorException {
    if (account != null && account.getAnalyticDistributionAuthorized()) {
      if (account.getAnalyticDistributionTemplate() == null
          && account.getCompany() != null
          && accountConfigService
                  .getAccountConfig(account.getCompany())
                  .getAnalyticDistributionTypeSelect()
              != AccountConfigRepository.DISTRIBUTION_TYPE_FREE) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get("Please put AnalyticDistribution Template"));

      } else {
        if (account.getAnalyticDistributionTemplate() != null) {
          if (account.getAnalyticDistributionTemplate().getAnalyticDistributionLineList() == null) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                I18n.get(
                    "Please put AnalyticDistributionLines in the Analytic Distribution Template"));
          } else {
            List<Long> rulesAnalyticAccountList = getRulesIds(account);
            if (rulesAnalyticAccountList != null && !rulesAnalyticAccountList.isEmpty()) {
              List<Long> accountAnalyticAccountList = new ArrayList<Long>();
              account
                  .getAnalyticDistributionTemplate()
                  .getAnalyticDistributionLineList()
                  .forEach(
                      analyticDistributionLine ->
                          accountAnalyticAccountList.add(
                              analyticDistributionLine.getAnalyticAccount().getId()));
              for (Long analyticAccount : accountAnalyticAccountList) {
                if (!rulesAnalyticAccountList.contains(analyticAccount)) {
                  throw new AxelorException(
                      TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                      I18n.get(
                          "The selected Analytic Distribution template contains Analytic Accounts which are not allowed on this account. Please select an appropriate template or modify the analytic coherence rule for this account."));
                }
              }
            }
          }
        }
      }
    }
  }

  public List<Long> getRulesIds(Account account) {
    Query query =
        JPA.em()
            .createQuery(
                "SELECT analyticAccount.id FROM AnalyticRules "
                    + "self JOIN self.analyticAccountSet analyticAccount "
                    + "WHERE self.fromAccount.code <= :account AND self.toAccount.code >= :account");
    query.setParameter("account", account.getCode());
    return query.getResultList();
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

  protected Account activate(Account account) {
    account.setStatusSelect(AccountRepository.STATUS_ACTIVE);
    return account;
  }

  protected Account desactivate(Account account) {
    account.setStatusSelect(AccountRepository.STATUS_INACTIVE);
    return account;
  }

  @Transactional
  public void updateMoveLineServiceType(Account account) {
    moveLineRepository
        .all()
        .filter("self.account = :account")
        .bind("account", account)
        .update("serviceType", account.getServiceType());
  }
}
