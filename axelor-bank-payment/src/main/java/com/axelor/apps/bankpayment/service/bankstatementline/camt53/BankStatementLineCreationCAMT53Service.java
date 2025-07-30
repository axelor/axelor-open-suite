package com.axelor.apps.bankpayment.service.bankstatementline.camt53;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.CashBalance3;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.ReportEntry2;
import com.axelor.apps.base.db.BankDetails;
import com.google.inject.persist.Transactional;

public interface BankStatementLineCreationCAMT53Service {
  @Transactional(rollbackOn = {Exception.class})
  int createBalanceLine(
      BankStatement bankStatement,
      BankDetails bankDetails,
      CashBalance3 balanceEntry,
      int sequence,
      String balanceTypeRequired,
      String currencyCodeFromStmt);

  @Transactional(rollbackOn = {Exception.class})
  int createEntryLine(
      BankStatement bankStatement,
      BankDetails bankDetails,
      ReportEntry2 ntry,
      int sequence,
      String currencyCodeFromStmt);
}
