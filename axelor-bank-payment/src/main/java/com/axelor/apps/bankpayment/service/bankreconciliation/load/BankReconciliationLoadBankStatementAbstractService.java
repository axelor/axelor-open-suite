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
package com.axelor.apps.bankpayment.service.bankreconciliation.load;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationComputeService;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public abstract class BankReconciliationLoadBankStatementAbstractService {

  protected BankReconciliationRepository bankReconciliationRepository;
  protected BankReconciliationComputeService bankReconciliationComputeService;

  @Inject
  public BankReconciliationLoadBankStatementAbstractService(
      BankReconciliationRepository bankReconciliationRepository,
      BankReconciliationComputeService bankReconciliationComputeService) {
    this.bankReconciliationRepository = bankReconciliationRepository;
    this.bankReconciliationComputeService = bankReconciliationComputeService;
  }

  public abstract void loadBankStatement(
      BankReconciliation bankReconciliation, boolean includeBankStatement);

  protected abstract void loadBankStatementLines(
      BankReconciliation bankReconciliation, boolean includeBankStatement);

  public void loadBankStatementAndCompute(
      BankReconciliation bankReconciliation, boolean includeBankStatement) {
    loadBankStatement(bankReconciliation, includeBankStatement);
    bankReconciliationComputeService.compute(bankReconciliation);
    bankReconciliationRepository.save(bankReconciliation);
  }

  protected List<Long> getExistingBankStatementLines(BankReconciliation bankReconciliation) {
    List<Long> bankStatementLineIds = Lists.newArrayList();
    List<BankReconciliationLine> bankReconciliationLines =
        bankReconciliation.getBankReconciliationLineList();
    if (CollectionUtils.isEmpty(bankReconciliationLines)) {
      return bankStatementLineIds;
    }
    for (BankReconciliationLine bankReconciliationLine : bankReconciliationLines) {
      if (bankReconciliationLine.getBankStatementLine() != null) {
        bankStatementLineIds.add(bankReconciliationLine.getBankStatementLine().getId());
      }
    }
    return bankStatementLineIds;
  }
}
