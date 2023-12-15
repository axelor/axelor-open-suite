package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class BankReconciliationSelectedLineComputationServiceImpl
    implements BankReconciliationSelectedLineComputationService {

  protected BankReconciliationQueryService bankReconciliationQueryService;
  protected MoveLineRepository moveLineRepository;

  @Inject
  public BankReconciliationSelectedLineComputationServiceImpl(
      BankReconciliationQueryService bankReconciliationQueryService,
      MoveLineRepository moveLineRepository) {
    this.bankReconciliationQueryService = bankReconciliationQueryService;
    this.moveLineRepository = moveLineRepository;
  }

  @Override
  public BigDecimal computeBankReconciliationLinesSelection(BankReconciliation bankReconciliation)
      throws AxelorException {

    return bankReconciliation.getBankReconciliationLineList().stream()
        .filter(BankReconciliationLine::getIsSelectedBankReconciliation)
        .map(it -> it.getCredit().subtract(it.getDebit()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Override
  public BigDecimal computeUnreconciledMoveLinesSelection(BankReconciliation bankReconciliation)
      throws AxelorException {
    String filter = bankReconciliationQueryService.getRequestMoveLines(bankReconciliation);
    filter = filter.concat(" AND self.isSelectedBankReconciliation = true");
    List<MoveLine> unreconciledMoveLines =
        moveLineRepository
            .all()
            .filter(filter)
            .bind(bankReconciliationQueryService.getBindRequestMoveLine(bankReconciliation))
            .fetch();
    return unreconciledMoveLines.stream()
        .filter(MoveLine::getIsSelectedBankReconciliation)
        .map(MoveLine::getCurrencyAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @Override
  public BigDecimal getSelectedMoveLineTotal(
      BankReconciliation bankReconciliation, List<LinkedHashMap> toReconcileMoveLineSet) {
    BigDecimal selectedMoveLineTotal = BigDecimal.ZERO;
    List<MoveLine> moveLineList = new ArrayList<>();
    toReconcileMoveLineSet.forEach(
        m ->
            moveLineList.add(
                moveLineRepository.find(
                    Long.valueOf((Integer) ((LinkedHashMap<?, ?>) m).get("id")))));
    for (MoveLine moveLine : moveLineList) {
      selectedMoveLineTotal = selectedMoveLineTotal.add(moveLine.getCurrencyAmount().abs());
    }
    return selectedMoveLineTotal;
  }
}
