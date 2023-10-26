package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementFileFormat;
import com.axelor.apps.bankpayment.db.EbicsPartner;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface BankStatementCreateService {

  BankStatement createBankStatement(
      File file,
      LocalDate fromDate,
      LocalDate toDate,
      BankStatementFileFormat bankStatementFileFormat,
      EbicsPartner ebicsPartner,
      LocalDateTime executionDateTime)
      throws IOException;

  String computeName(BankStatement bankStatement);
}
