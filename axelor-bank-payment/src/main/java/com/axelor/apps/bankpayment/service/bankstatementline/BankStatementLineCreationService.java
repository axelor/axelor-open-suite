package com.axelor.apps.bankpayment.service.bankstatementline;

import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface BankStatementLineCreationService {
  BankStatementLine createBankStatementLine(
      BankStatement bankStatement,
      int sequence,
      BankDetails bankDetails,
      BigDecimal debit,
      BigDecimal credit,
      Currency currency,
      String description,
      LocalDate operationDate,
      LocalDate valueDate,
      InterbankCodeLine operationInterbankCodeLine,
      InterbankCodeLine rejectInterbankCodeLine,
      String origin,
      String reference);
}
