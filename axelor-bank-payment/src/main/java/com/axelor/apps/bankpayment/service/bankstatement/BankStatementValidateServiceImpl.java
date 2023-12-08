package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public class BankStatementValidateServiceImpl implements BankStatementValidateService {
  BankStatementRepository bankStatementRepository;
  protected BankStatementLineRepository bankStatementLineRepository;

  @Inject
  public BankStatementValidateServiceImpl(
      BankStatementRepository bankStatementRepository,
      BankStatementLineRepository bankStatementLineRepository) {
    this.bankStatementRepository = bankStatementRepository;
    this.bankStatementLineRepository = bankStatementLineRepository;
  }

  @Transactional
  public BankStatement setIsFullyReconciled(BankStatement bankStatement) {
    List<BankStatementLine> bankStatementLines =
        bankStatementLineRepository.findByBankStatement(bankStatement).fetch();
    BigDecimal amountToReconcile = BigDecimal.ZERO;
    for (BankStatementLine bankStatementLine : bankStatementLines) {
      amountToReconcile = amountToReconcile.add(bankStatementLine.getAmountRemainToReconcile());
    }
    if (amountToReconcile.compareTo(BigDecimal.ZERO) == 0) {
      bankStatement.setIsFullyReconciled(true);
    }

    return bankStatementRepository.save(bankStatement);
  }
}
