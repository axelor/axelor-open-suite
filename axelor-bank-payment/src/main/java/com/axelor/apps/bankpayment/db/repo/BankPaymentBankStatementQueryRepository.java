package com.axelor.apps.bankpayment.db.repo;

import com.axelor.apps.bankpayment.db.BankStatementQuery;
import com.axelor.db.Query;
import java.util.HashMap;
import java.util.Map;

public class BankPaymentBankStatementQueryRepository extends BankStatementQueryRepository {
  public BankStatementQuery findBySequenceAndRuleTypeExcludeId(
      Integer sequence, Integer ruleType, Long id) {
    String query = "self.sequence = :sequence AND self.ruleType = :ruleType";
    Map<String, Object> binding = new HashMap<String, Object>();
    binding.put("sequence", sequence);
    binding.put("ruleType", ruleType);
    if (id != null) {
      query = query + "AND self.id != :id";
      binding.put("id", id);
    }
    return Query.of(BankStatementQuery.class).filter(query).bind(binding).fetchOne();
  }
}
