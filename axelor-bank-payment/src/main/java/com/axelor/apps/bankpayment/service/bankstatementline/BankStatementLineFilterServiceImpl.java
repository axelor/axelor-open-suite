package com.axelor.apps.bankpayment.service.bankstatementline;

public class BankStatementLineFilterServiceImpl implements BankStatementLineFilterService {

  /**
   * Write the filter for the bank statement line query, depending on boolean parameters.
   *
   * @param includeOtherBankStatements whether we include other bank statement.
   * @param includeBankStatement whether we include the bank statement given in parameter. this
   *     parameter cannot be false if includeOtherBankstatements is false.
   * @return the filter.
   */
  @Override
  public String getBankStatementLinesFilter(
      boolean includeOtherBankStatements, boolean includeBankStatement) {

    String filter =
        "self.bankDetails = :bankDetails"
            + " and self.currency = :currency"
            + " and self.bankStatement.statusSelect = :statusImported";

    if (!includeOtherBankStatements && includeBankStatement) {
      filter += " and self.bankStatement = :bankStatement";
    } else if (includeOtherBankStatements && includeBankStatement) {
      filter += " and self.bankStatement.bankStatementFileFormat = :bankStatementFileFormat";
    } else {
      filter +=
          " and self.bankStatement.bankStatementFileFormat = :bankStatementFileFormat"
              + " and self.bankStatement != :bankStatement";
    }

    return filter;
  }

  /**
   * Write the filter for the bank statement line query, depending on boolean parameters. Add a
   * filter on lineTypeSelect compared to version from super.
   *
   * @param includeOtherBankStatements whether we include other bank statement.
   * @param includeBankStatement whether we include the bank statement given in parameter. this
   *     parameter cannot be false if includeOtherBankstatements is false.
   * @return the filter.
   */
  @Override
  public String getBankStatementLinesAFB120Filter(
      boolean includeOtherBankStatements, boolean includeBankStatement) {

    return getBankStatementLinesFilter(includeOtherBankStatements, includeBankStatement)
        + " and self.lineTypeSelect = :lineTypeSelect";
  }

  @Override
  public String getBankStatementLinesFilterWithAmountToReconcile(
      boolean includeOtherBankStatements, boolean includeBankStatement) {

    return getBankStatementLinesFilter(includeOtherBankStatements, includeBankStatement)
        + " and self.amountRemainToReconcile > 0";
  }

  @Override
  public String getBankStatementLinesAFB120FilterWithAmountToReconcile(
      boolean includeOtherBankStatements, boolean includeBankStatement) {

    return getBankStatementLinesAFB120Filter(includeOtherBankStatements, includeBankStatement)
        + " and self.amountRemainToReconcile > 0";
  }
}
