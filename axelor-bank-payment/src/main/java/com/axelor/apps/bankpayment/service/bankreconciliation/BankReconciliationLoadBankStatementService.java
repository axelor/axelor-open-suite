package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementFileFormat;
import com.axelor.apps.bankpayment.db.repo.BankStatementFileFormatRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.load.BankReconciliationLoadBankStatementClassicService;
import com.axelor.apps.bankpayment.service.bankreconciliation.load.afb120.BankReconciliationLoadBankStatementAFB120Service;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class BankReconciliationLoadBankStatementService {

  @Transactional
  public void loadBankStatement(
      BankReconciliation bankReconciliation, boolean includeBankStatement) {

    BankStatement bankStatement = bankReconciliation.getBankStatement();
    BankStatementFileFormat bankStatementFileFormat = bankStatement.getBankStatementFileFormat();

    switch (bankStatementFileFormat.getStatementFileFormatSelect()) {
      case BankStatementFileFormatRepository.FILE_FORMAT_CAMT_XXX_CFONB120_REP:
      case BankStatementFileFormatRepository.FILE_FORMAT_CAMT_XXX_CFONB120_STM:
        Beans.get(BankReconciliationLoadBankStatementAFB120Service.class)
            .loadBankStatementAndCompute(bankReconciliation, includeBankStatement);
        break;

      default:
        Beans.get(BankReconciliationLoadBankStatementClassicService.class)
            .loadBankStatementAndCompute(bankReconciliation, includeBankStatement);
    }
  }
}
