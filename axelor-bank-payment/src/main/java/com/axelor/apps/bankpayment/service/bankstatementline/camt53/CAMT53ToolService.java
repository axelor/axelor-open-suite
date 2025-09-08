package com.axelor.apps.bankpayment.service.bankstatementline.camt53;

import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.AccountStatement2;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.CashAccount20;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.CashBalance3;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.DateAndDateTimeChoice;
import com.axelor.apps.bankpayment.xsd.bankstatement.camt_053_001_02.ReportEntry2;
import com.axelor.apps.base.db.BankDetails;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CAMT53ToolService {
  void computeBankStatementDates(BankStatement bankStatement, List<AccountStatement2> stmtList);

  LocalDate computeLocalDateFromDateTimeChoice(DateAndDateTimeChoice dateTimeChoice);

  String getBalanceType(CashBalance3 balanceEntry);

  String getCreditDebitIndicatorFromReportEntry(ReportEntry2 ntry);

  String getCreditDebitIndicatorFromCashEntry(CashBalance3 balanceEntry);

  BigDecimal getReportEntryValue(ReportEntry2 ntry);

  BigDecimal getCashEntryValue(CashBalance3 balance);

  String getReference(ReportEntry2 ntry);

  Integer getCommissionExemptionIndexSelect(ReportEntry2 ntry);

  BankDetails findBankDetailsByIBAN(CashAccount20 acct);

  InterbankCodeLine getOperationCodeInterBankCodeLineCode(ReportEntry2 ntry);

  InterbankCodeLine getRejectReturnInterBankCodeLineCode(ReportEntry2 reportEntry2);
}
