package com.axelor.apps.bankpayment.service.bankstatementline.camt53;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.CashBalance3;
import com.axelor.apps.bankpayment.xsd.sepa.camt_053_001_02.ReportEntry2;
import com.axelor.apps.base.db.BankDetails;

public interface BankStatementLineCreationCAMT53Service {
  int createBalanceLine(
      BankStatement bankStatement,
      BankDetails bankDetails,
      CashBalance3 balanceEntry,
      int sequence,
      String balanceTypeRequired,
      String currencyCodeFromStmt);

  int createEntryLine(
      BankStatement bankStatement,
      BankDetails bankDetails,
      ReportEntry2 ntry,
      int sequence,
      String currencyCodeFromStmt);
}
