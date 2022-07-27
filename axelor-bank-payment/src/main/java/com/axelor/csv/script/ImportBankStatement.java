package com.axelor.csv.script;

import com.axelor.apps.bankpayment.db.BankStatement;
import java.util.Map;

public class ImportBankStatement {

  public Object importBankStatement(Object bean, Map<String, Object> values) {
    assert bean instanceof BankStatement;
    BankStatement bankStatement = (BankStatement) bean;

    if (bankStatement.getName() == null) {
      bankStatement.setName(computeName(bankStatement));
    }
    return bankStatement;
  }

  private String computeName(BankStatement bankStatement) {
    StringBuilder builder = new StringBuilder(bankStatement.getBankStatementFileFormat().getName());
    builder.append(" - ");
    builder.append(bankStatement.getFromDate().toString());
    builder.append(" - ");
    builder.append(bankStatement.getToDate().toString());
    return builder.toString();
  }
}
