package com.axelor.apps.bankpayment.service.bankstatementquery;

import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementQuery;
import com.axelor.exception.AxelorException;

public interface BankStatementQueryService {

  /**
   * Evaluate the bankstatementQuery and return the resulting object. Only works with
   * bankStatementQuery of type Partner's fetching (2) and Invoice's fetching (3). It can not be
   * used with type Accounting auto (0) or Reconciliation auto (1).
   *
   * @param bankStatementQuery: can not be null
   * @param bankStatementLine : will be used as context, can not be null
   * @return the generated object (either a Invoice or Partner)
   * @throws AxelorException
   */
  Object evalQuery(BankStatementQuery bankStatementQuery, BankStatementLine bankStatementLine)
      throws AxelorException;
}
