package com.axelor.apps.bankpayment.service.moveline;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class MoveLineRecordBankPaymentServiceImpl implements MoveLineRecordBankPaymentService {
  MoveLineToolBankPaymentService moveLineToolBankPaymentService;
  MoveLineRepository moveLineRepo;

  @Inject
  public MoveLineRecordBankPaymentServiceImpl(
      MoveLineToolBankPaymentService moveLineToolBankPaymentService,
      MoveLineRepository moveLineRepo) {
    this.moveLineToolBankPaymentService = moveLineToolBankPaymentService;
    this.moveLineRepo = moveLineRepo;
  }

  @Override
  public void revertDebitCreditAmountChange(MoveLine moveLine) {
    if (!moveLineToolBankPaymentService.checkBankReconciledAmount(moveLine)) {
      return;
    }

    if (moveLine.getId() == null) {
      MoveLine savedMoveLine = moveLineRepo.find(moveLine.getId());
      moveLine.setDebit(savedMoveLine.getDebit());
      moveLine.setCredit(savedMoveLine.getCredit());
    } else {
      moveLine.setDebit(BigDecimal.ZERO);
      moveLine.setCredit(BigDecimal.ZERO);
    }
  }

  @Override
  public void revertBankReconciledAmountChange(MoveLine moveLine) {
    if (!moveLineToolBankPaymentService.checkBankReconciledAmount(moveLine)) {
      return;
    }

    if (moveLine.getId() == null) {
      MoveLine savedMoveLine = moveLineRepo.find(moveLine.getId());
      moveLine.setBankReconciledAmount(savedMoveLine.getBankReconciledAmount());
    } else {
      moveLine.setBankReconciledAmount(BigDecimal.ZERO);
    }
  }
}
