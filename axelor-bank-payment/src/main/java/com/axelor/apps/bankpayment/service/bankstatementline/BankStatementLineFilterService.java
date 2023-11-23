package com.axelor.apps.bankpayment.service.bankstatementline;

public interface BankStatementLineFilterService {

  String getBankStatementLinesFilter(
      boolean includeOtherBankStatements, boolean includeBankStatement);

  String getBankStatementLinesAFB120Filter(
      boolean includeOtherBankStatements, boolean includeBankStatement);

  String getBankStatementLinesFilterWithAmountToReconcile(
      boolean includeOtherBankStatements, boolean includeBankStatement);

  String getBankStatementLinesAFB120FilterWithAmountToReconcile(
      boolean includeOtherBankStatements, boolean includeBankStatement);
}
