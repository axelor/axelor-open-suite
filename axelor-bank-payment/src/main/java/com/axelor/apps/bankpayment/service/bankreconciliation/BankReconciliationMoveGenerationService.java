package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatementRule;
import com.axelor.apps.base.AxelorException;

public interface BankReconciliationMoveGenerationService {

  void generateMovesAutoAccounting(BankReconciliation bankReconciliation) throws AxelorException;

  Move generateMove(
      BankReconciliationLine bankReconciliationLine, BankStatementRule bankStatementRule)
      throws AxelorException;

  void checkAccountBeforeAutoAccounting(
      BankStatementRule bankStatementRule, BankReconciliation bankReconciliation)
      throws AxelorException;
}
