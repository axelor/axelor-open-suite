package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;

public interface BankReconciliationSelectedLineComputationService {

  BigDecimal computeBankReconciliationLinesSelection(BankReconciliation bankReconciliation)
      throws AxelorException;

  BigDecimal computeUnreconciledMoveLinesSelection(BankReconciliation bankReconciliation)
      throws AxelorException;

  BigDecimal getSelectedMoveLineTotal(
      BankReconciliation bankReconciliation, List<LinkedHashMap> toReconcileMoveLineSet);
}
