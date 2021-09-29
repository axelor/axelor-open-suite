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
      Integer sequence, Integer ruleTypeSelect, Long id) {
    String query = "self.sequence = :sequence AND self.ruleTypeSelect = :ruleTypeSelect";
    Map<String, Object> binding = new HashMap<String, Object>();
    binding.put("sequence", sequence);
    binding.put("ruleTypeSelect", ruleTypeSelect);
    if (id != null) {
      query = query + " AND self.id != :id";
      binding.put("id", id);
    }
    return bankStatementQueryRepository.all().filter(query).bind(binding).fetchOne();
  }
}
