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
