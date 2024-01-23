package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.base.AxelorException;
import java.util.Map;

public interface BankReconciliationQueryService {
  String getRequestMoveLines(BankReconciliation bankReconciliation);

  Map<String, Object> getBindRequestMoveLine(BankReconciliation bankReconciliation)
      throws AxelorException;
}
