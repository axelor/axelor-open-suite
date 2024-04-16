package com.axelor.apps.bankpayment.db.repo;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLineCAMT53;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.db.Query;

public class BankPaymentBankStatementLineCAMT53Repository
    extends BankStatementLineCAMT53Repository {
  public Query<BankStatementLineCAMT53> findByBankStatementBankDetailsAndLineType(
      BankStatement bankStatement, BankDetails bankDetails, int lineType) {
    return all()
        .filter(
            "self.bankStatement = :bankStatement"
                + " AND self.bankDetails = :bankDetails"
                + " AND self.lineTypeSelect = :lineTypeSelect")
        .bind("bankStatement", bankStatement)
        .bind("bankDetails", bankDetails)
        .bind("lineTypeSelect", lineType);
  }

  public Query<BankStatementLineCAMT53> findByBankDetailsLineTypeExcludeBankStatement(
      BankStatement bankStatement, BankDetails bankDetails, int lineType) {
    return all()
        .filter(
            "self.bankStatement != :bankStatement"
                + " AND self.bankDetails = :bankDetails"
                + " AND self.lineTypeSelect = :lineTypeSelect")
        .bind("bankStatement", bankStatement)
        .bind("bankDetails", bankDetails)
        .bind("lineTypeSelect", lineType);
  }
}
