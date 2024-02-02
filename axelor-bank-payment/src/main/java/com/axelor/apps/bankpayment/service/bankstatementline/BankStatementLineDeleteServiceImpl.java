package com.axelor.apps.bankpayment.service.bankstatementline;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class BankStatementLineDeleteServiceImpl implements BankStatementLineDeleteService {

  protected BankStatementLineRepository bankStatementLineRepository;

  @Inject
  public BankStatementLineDeleteServiceImpl(
      BankStatementLineRepository bankStatementLineRepository) {
    this.bankStatementLineRepository = bankStatementLineRepository;
  }

  @Transactional
  public void deleteBankStatementLines(BankStatement bankStatement) {
    List<BankStatementLine> bankStatementLines;
    bankStatementLines =
        bankStatementLineRepository
            .all()
            .filter("self.bankStatement = :bankStatement")
            .bind("bankStatement", bankStatement)
            .fetch();
    for (BankStatementLine bsl : bankStatementLines) {
      bankStatementLineRepository.remove(bsl);
    }
  }
}
