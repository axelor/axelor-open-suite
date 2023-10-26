package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import java.io.IOException;
import java.util.List;

public interface BankStatementService {

  void runImport(BankStatement bankStatement, boolean alertIfFormatNotSupported)
      throws IOException, AxelorException;

  BankStatement find(BankStatement bankStatement);

  void deleteBankStatementLines(BankStatement bankStatement);

  BankStatement setIsFullyReconciled(BankStatement bankStatement);

  List<BankDetails> fetchBankDetailsList(BankStatement bankStatement);

  void updateBankDetailsBalanceAndDate(List<BankDetails> bankDetails);

  List<BankStatementLineAFB120> getBankStatementLines(BankStatement bankStatement);
}
