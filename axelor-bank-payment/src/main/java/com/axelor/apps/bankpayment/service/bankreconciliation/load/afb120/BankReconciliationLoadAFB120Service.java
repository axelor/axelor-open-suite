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
package com.axelor.apps.bankpayment.service.bankreconciliation.load.afb120;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationLineService;
import com.axelor.apps.bankpayment.service.bankreconciliation.load.BankReconciliationLoadService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class BankReconciliationLoadAFB120Service extends BankReconciliationLoadService {

  protected BankStatementLineAFB120Repository bankStatementLineAFB120Repository;

  @Inject
  public BankReconciliationLoadAFB120Service(
      BankReconciliationLineService bankReconciliationLineService,
      BankStatementLineAFB120Repository bankStatementLineAFB120Repository) {

    super(bankReconciliationLineService);

    this.bankStatementLineAFB120Repository = bankStatementLineAFB120Repository;
  }

  @Transactional
  public void loadBankStatement(
      BankReconciliation bankReconciliation, boolean includeBankStatement) {
    BankStatementLine initialBalanceBankStatementLine =
        getInitialBalanceBankStatementLine(bankReconciliation, includeBankStatement);
    BankStatementLine finalBalanceBankStatementLine =
        getFinalBalanceBankStatementLine(bankReconciliation, includeBankStatement);

    if (initialBalanceBankStatementLine != null) {
      bankReconciliation.setStartingBalance(
          initialBalanceBankStatementLine
              .getCredit()
              .subtract(initialBalanceBankStatementLine.getDebit()));
    }
    if (finalBalanceBankStatementLine != null) {
      bankReconciliation.setEndingBalance(
          finalBalanceBankStatementLine
              .getCredit()
              .subtract(finalBalanceBankStatementLine.getDebit()));
    }

    this.loadBankStatementLines(bankReconciliation, includeBankStatement);
  }

  @Transactional
  public void loadBankStatementLines(
      BankReconciliation bankReconciliation, boolean includeBankStatement) {

    List<BankStatementLineAFB120> bankStatementLineList =
        getBankStatementLinesAFB120(bankReconciliation, includeBankStatement);

    if (bankStatementLineList != null) {
      for (BankStatementLine bankStatementLine : bankStatementLineList) {
        if (bankStatementLine.getAmountRemainToReconcile().compareTo(BigDecimal.ZERO) != 0) {
          bankReconciliation.addBankReconciliationLineListItem(
              bankReconciliationLineService.createBankReconciliationLine(bankStatementLine));
        }
      }
    }
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
  protected String getBankStatementLinesFilter(
      boolean includeOtherBankStatements,
      boolean includeBankStatement,
      boolean isLineTypeMovement) {

    return super.getBankStatementLinesFilter(
            includeOtherBankStatements, includeBankStatement, isLineTypeMovement)
        + " and self.lineTypeSelect = :lineTypeSelect";
  }

  protected List<BankStatementLineAFB120> getBankStatementLinesAFB120(
      BankReconciliation bankReconciliation, boolean includeBankStatement) {

    BankStatement bankStatement = bankReconciliation.getBankStatement();
    String queryFilter =
        getBankStatementLinesFilter(
            bankReconciliation.getIncludeOtherBankStatements(), includeBankStatement, true);
    Query<BankStatementLineAFB120> bankStatementLinesQuery =
        JPA.all(BankStatementLineAFB120.class)
            .bind("bankDetails", bankReconciliation.getBankDetails())
            .bind("currency", bankReconciliation.getCurrency())
            .bind("statusImported", BankStatementRepository.STATUS_IMPORTED)
            .bind("bankStatement", bankStatement)
            .bind("bankStatementFileFormat", bankStatement.getBankStatementFileFormat())
            .bind("lineTypeSelect", BankStatementLineAFB120Repository.LINE_TYPE_MOVEMENT)
            .order("valueDate")
            .order("sequence");
    List<Long> existingBankStatementLineIds = getExistingBankStatementLines(bankReconciliation);
    if (!CollectionUtils.isEmpty(existingBankStatementLineIds)) {
      queryFilter += " AND self.id NOT IN (:existingBankStatementLines)";
      bankStatementLinesQuery.bind("existingBankStatementLines", existingBankStatementLineIds);
    }
    return bankStatementLinesQuery.filter(queryFilter).fetch();
  }

  protected BankStatementLine getInitialBalanceBankStatementLine(
      BankReconciliation bankReconciliation, boolean includeBankStatement) {

    BankStatement bankStatement = bankReconciliation.getBankStatement();

    return JPA.all(BankStatementLineAFB120.class)
        .filter(
            getBankStatementLinesFilter(
                bankReconciliation.getIncludeOtherBankStatements(), includeBankStatement, false))
        .bind("bankDetails", bankReconciliation.getBankDetails())
        .bind("currency", bankReconciliation.getCurrency())
        .bind("statusImported", BankStatementRepository.STATUS_IMPORTED)
        .bind("bankStatement", bankStatement)
        .bind("bankStatementFileFormat", bankStatement.getBankStatementFileFormat())
        .bind("lineTypeSelect", BankStatementLineAFB120Repository.LINE_TYPE_INITIAL_BALANCE)
        .order("operationDate")
        .order("sequence")
        .fetchOne();
  }

  protected BankStatementLine getFinalBalanceBankStatementLine(
      BankReconciliation bankReconciliation, boolean includeBankStatement) {

    BankStatement bankStatement = bankReconciliation.getBankStatement();

    return JPA.all(BankStatementLineAFB120.class)
        .filter(
            getBankStatementLinesFilter(
                bankReconciliation.getIncludeOtherBankStatements(), includeBankStatement, false))
        .bind("bankDetails", bankReconciliation.getBankDetails())
        .bind("currency", bankReconciliation.getCurrency())
        .bind("statusImported", BankStatementRepository.STATUS_IMPORTED)
        .bind("bankStatement", bankStatement)
        .bind("bankStatementFileFormat", bankStatement.getBankStatementFileFormat())
        .bind("lineTypeSelect", BankStatementLineAFB120Repository.LINE_TYPE_FINAL_BALANCE)
        .order("-operationDate")
        .order("-sequence")
        .fetchOne();
  }
}
