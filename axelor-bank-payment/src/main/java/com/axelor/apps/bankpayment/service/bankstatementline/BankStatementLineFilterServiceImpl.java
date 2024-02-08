/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
