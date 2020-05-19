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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.db.JPA;
import com.google.inject.Inject;
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

  @Inject
  public AccountService(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
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

    return accountRepository
        .all()
        .filter("self.parentAccount.id = ?1", accountId)
        .select("id")
        .fetch(0, 0)
        .stream()
        .map(m -> (Long) m.get("id"))
        .collect(Collectors.toList());
  }
}
