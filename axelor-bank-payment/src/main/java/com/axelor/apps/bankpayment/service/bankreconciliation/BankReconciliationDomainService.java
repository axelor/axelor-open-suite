package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;

public interface BankReconciliationDomainService {

  String getDomainForWizard(
      BankReconciliation bankReconciliation,
      BigDecimal bankStatementCredit,
      BigDecimal bankStatementDebit);

  String getAccountDomain(BankReconciliation bankReconciliation);

  String getCashAccountDomain(BankReconciliation bankReconciliation);

  String createDomainForMoveLine(BankReconciliation bankReconciliation) throws AxelorException;

  String getJournalDomain(BankReconciliation bankReconciliation);

  String createDomainForBankDetails(BankReconciliation bankReconciliation);
}
