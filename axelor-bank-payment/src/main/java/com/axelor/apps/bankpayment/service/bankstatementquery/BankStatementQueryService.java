package com.axelor.apps.bankpayment.service.bankstatementquery;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementQuery;
import com.axelor.exception.AxelorException;

public interface BankStatementQueryService {

  /**
   * Evaluate the bankstatementQuery and return the resulting object. Only works with
   * bankStatementQuery of type Partner's fetching (2) and Move line's fetching (3). It can not be
   * used with type Accounting auto (0) or Reconciliation auto (1).
   *
   * @param bankStatementQuery: can not be null
   * @param bankStatementLine : will be used as context, can not be null
   * @param move: will be usable in context, can be null if type 2
   * @return the generated object (either a MoveLine or Partner)
   * @throws AxelorException
   */
  Object evalQuery(
      BankStatementQuery bankStatementQuery, BankStatementLine bankStatementLine, Move move)
      throws AxelorException;
}
