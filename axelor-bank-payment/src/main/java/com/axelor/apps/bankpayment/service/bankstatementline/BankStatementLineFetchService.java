package com.axelor.apps.bankpayment.service.bankstatementline;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.base.db.BankDetails;
import java.util.List;

public interface BankStatementLineFetchService {
  List<BankStatementLine> getBankStatementLines(BankStatement bankStatement);

  List<BankDetails> getBankDetailsFromStatementLines(BankStatement bankStatement);
}
