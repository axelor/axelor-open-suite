package com.axelor.apps.bankpayment.db.repo;

import com.axelor.apps.bankpayment.db.BankStatementQuery;
import com.axelor.db.Query;

public class BankPaymentBankStatementQueryRepository extends BankStatementQueryRepository {
  public BankStatementQuery findBySequenceAndRuleTypeExcludeId(
      Integer sequence, Integer ruleType, Long id) {
    return Query.of(BankStatementQuery.class)
        .filter("self.sequence = :sequence AND self.ruleType = :ruleType AND self.id != :id")
        .bind("sequence", sequence)
        .bind("ruleType", ruleType)
        .bind("id", id)
        .fetchOne();
  }
}
