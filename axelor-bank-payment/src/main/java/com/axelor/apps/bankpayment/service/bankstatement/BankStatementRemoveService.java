package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import java.util.List;

public interface BankStatementRemoveService {

  void deleteStatement(BankStatement bankStatement) throws Exception;

  int deleteMultiple(List<Long> idList);
}
