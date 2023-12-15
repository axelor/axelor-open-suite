package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.base.AxelorException;
import java.util.List;

public interface BankReconciliationReconciliationService {

  BankReconciliation reconciliateAccordingToQueries(BankReconciliation bankReconciliation)
      throws AxelorException;

  void checkReconciliation(List<MoveLine> moveLines, BankReconciliation br) throws AxelorException;

  BankReconciliation reconcileSelected(BankReconciliation bankReconciliation)
      throws AxelorException;
}
