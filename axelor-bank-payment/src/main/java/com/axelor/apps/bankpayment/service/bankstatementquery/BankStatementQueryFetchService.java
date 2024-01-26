/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
