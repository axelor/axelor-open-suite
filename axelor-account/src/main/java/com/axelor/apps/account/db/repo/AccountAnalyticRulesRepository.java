package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticRules;
import com.axelor.db.Query;
import java.util.List;

public class AccountAnalyticRulesRepository extends AnalyticRulesRepository {

  public List<AnalyticRules> findByAccounts(Account account) {
    return Query.of(AnalyticRules.class)
        .filter("self.fromAccount.code <= :account AND self.toAccount.code >= :account")
        .bind("account", account.getCode())
        .fetch();
  }
}
