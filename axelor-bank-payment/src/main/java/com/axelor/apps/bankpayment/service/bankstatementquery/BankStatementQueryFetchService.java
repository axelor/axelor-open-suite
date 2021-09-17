package com.axelor.apps.bankpayment.service.bankstatementquery;

import com.axelor.apps.bankpayment.db.BankStatementQuery;
import com.axelor.apps.bankpayment.db.repo.BankStatementQueryRepository;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class BankStatementQueryFetchService {
  protected BankStatementQueryRepository bankStatementQueryRepository;

  @Inject
  public BankStatementQueryFetchService(BankStatementQueryRepository bankStatementQueryRepository) {
    this.bankStatementQueryRepository = bankStatementQueryRepository;
  }

  public BankStatementQuery findBySequenceAndRuleTypeExcludeId(
      Integer sequence, Integer ruleType, Long id) {
    String query = "self.sequence = :sequence AND self.ruleType = :ruleType";
    Map<String, Object> binding = new HashMap<String, Object>();
    binding.put("sequence", sequence);
    binding.put("ruleType", ruleType);
    if (id != null) {
      query = query + " AND self.id != :id";
      binding.put("id", id);
    }
    return bankStatementQueryRepository.all().filter(query).bind(binding).fetchOne();
  }
}
