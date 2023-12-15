package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface BankReconciliationLineService {

  BankReconciliationLine createBankReconciliationLine(
      LocalDate effectDate,
      BigDecimal debit,
      BigDecimal credit,
      String name,
      String reference,
      BankStatementLine bankStatementLine,
      MoveLine moveLine);

  BankReconciliationLine createBankReconciliationLine(BankStatementLine bankStatementLine);

  void checkAmount(BankReconciliationLine bankReconciliationLine) throws AxelorException;

  BankReconciliationLine reconcileBRLAndMoveLine(
      BankReconciliationLine bankReconciliationLine, MoveLine moveLine);

  void updateBankReconciledAmounts(BankReconciliationLine bankReconciliationLine);

  void unreconcileLines(List<BankReconciliationLine> bankReconciliationLines);

  void unreconcileLine(BankReconciliationLine bankReconciliationLine);

  BankReconciliationLine setSelected(BankReconciliationLine bankReconciliationLineContext);

  void checkIncompleteLine(BankReconciliationLine bankReconciliationLine) throws AxelorException;

  MoveLine setMoveLinePostedNbr(MoveLine moveLine, String postedNbr);
}
