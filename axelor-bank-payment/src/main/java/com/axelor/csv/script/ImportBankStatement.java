package com.axelor.csv.script;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementCreateService;
import java.util.Map;
import javax.inject.Inject;

public class ImportBankStatement {

  protected BankStatementCreateService bankStatementCreateService;

  @Inject
  public ImportBankStatement(BankStatementCreateService bankStatementCreateService) {
    this.bankStatementCreateService = bankStatementCreateService;
  }

  public Object importBankStatement(Object bean, Map<String, Object> values) {
    assert bean instanceof BankStatement;
    BankStatement bankStatement = (BankStatement) bean;

    if (bankStatement.getName() == null) {
      bankStatement.setName(bankStatementCreateService.computeName(bankStatement));
    }
    return bankStatement;
  }
}
