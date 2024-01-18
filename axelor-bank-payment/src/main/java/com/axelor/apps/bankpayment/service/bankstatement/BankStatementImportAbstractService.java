package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;

public abstract class BankStatementImportAbstractService {

  protected BankStatementRepository bankStatementRepository;

  @Inject
  protected BankStatementImportAbstractService(BankStatementRepository bankStatementRepository) {
    this.bankStatementRepository = bankStatementRepository;
  }

  public abstract void runImport(BankStatement bankStatement) throws AxelorException, IOException;

  protected abstract void checkImport(BankStatement bankStatement)
      throws AxelorException, IOException;

  protected abstract void updateBankDetailsBalance(BankStatement bankStatement);

  @Transactional
  public void setBankStatementImported(BankStatement bankStatement) {
    bankStatement.setStatusSelect(BankStatementRepository.STATUS_IMPORTED);
    bankStatementRepository.save(bankStatement);
  }
}
