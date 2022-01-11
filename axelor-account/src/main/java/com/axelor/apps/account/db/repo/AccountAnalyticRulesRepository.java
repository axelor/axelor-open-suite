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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticRules;
import com.axelor.db.Query;
import java.util.ArrayList;
import java.util.List;

public class AccountAnalyticRulesRepository extends AnalyticRulesRepository {

  public List<AnalyticRules> findByAccounts(Account account) {
    return Query.of(AnalyticRules.class)
        .filter("self.fromAccount.code <= :account AND self.toAccount.code >= :account")
        .bind("account", account.getCode())
        .fetch();
  }

  public List<AnalyticAccount> findAnalyticAccountByAccounts(Account account) {
    List<AnalyticAccount> analyticAccountList = new ArrayList<AnalyticAccount>();
    List<AnalyticRules> analyticRulesList = findByAccounts(account);

    if (analyticRulesList != null && !analyticRulesList.isEmpty()) {
      for (AnalyticRules analyticRule : analyticRulesList) {
        if (analyticRule.getAnalyticAccountSet() != null) {
          for (AnalyticAccount analyticAccount : analyticRule.getAnalyticAccountSet()) {
            analyticAccountList.add(analyticAccount);
          }
        }
      }
    }
    return analyticAccountList;
  }
}
