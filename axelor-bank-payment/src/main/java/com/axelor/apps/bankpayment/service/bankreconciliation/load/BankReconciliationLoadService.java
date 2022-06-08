/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.service.bankreconciliation.load;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationLineService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class BankReconciliationLoadService {

  protected BankReconciliationLineService bankReconciliationLineService;

  @Inject
  public BankReconciliationLoadService(
      BankReconciliationLineService bankReconciliationLineService) {

    this.bankReconciliationLineService = bankReconciliationLineService;
  }

  @Transactional
  public void loadBankStatement(
      BankReconciliation bankReconciliation, boolean includeBankStatement) {

    this.loadBankStatementLines(bankReconciliation, includeBankStatement);
  }

  @Transactional
  public void loadBankStatementLines(BankReconciliation bankReconciliation) {
    loadBankStatementLines(bankReconciliation, true);
  }

  @Transactional
  public void loadBankStatementLines(
      BankReconciliation bankReconciliation, boolean includeBankStatement) {

    List<BankStatementLine> bankStatementLineList =
        getBankStatementLines(bankReconciliation, includeBankStatement);

    if (bankStatementLineList != null) {
      for (BankStatementLine bankStatementLine : bankStatementLineList) {

        bankReconciliation.addBankReconciliationLineListItem(
            bankReconciliationLineService.createBankReconciliationLine(bankStatementLine));
      }
    }
  }

  /**
   * Write the filter for the bank statement line query, depending on boolean parameters.
   *
   * @param includeOtherBankStatements whether we include other bank statement.
   * @param includeBankStatement whether we include the bank statement given in parameter. this
   *     parameter cannot be false if includeOtherBankstatements is false.
   * @return the filter.
   */
  protected String getBankStatementLinesFilter(
      boolean includeOtherBankStatements, boolean includeBankStatement) {

    String filter;
    if (!includeOtherBankStatements && includeBankStatement) {
      filter =
          "self.bankDetails = :bankDetails"
              + " and self.currency = :currency"
              + " and self.amountRemainToReconcile > 0"
              + " and self.bankStatement.statusSelect = :statusImported"
              + " and self.bankStatement = :bankStatement";
    } else if (includeOtherBankStatements && includeBankStatement) {
      filter =
          "self.bankDetails = :bankDetails"
              + " and self.currency = :currency"
              + " and self.amountRemainToReconcile > 0"
              + " and self.bankStatement.statusSelect = :statusImported"
              + " and self.bankStatement.bankStatementFileFormat = :bankStatementFileFormat";
    } else {
      filter =
          "self.bankDetails = :bankDetails"
              + " and self.currency = :currency"
              + " and self.amountRemainToReconcile > 0"
              + " and self.bankStatement.statusSelect = :statusImported"
              + " and self.bankStatement.bankStatementFileFormat = :bankStatementFileFormat"
              + " and self.bankStatement != :bankStatement";
    }
    return filter;
  }

  protected List<BankStatementLine> getBankStatementLines(
      BankReconciliation bankReconciliation, boolean includeBankStatement) {

    BankStatement bankStatement = bankReconciliation.getBankStatement();
    String queryFilter =
        getBankStatementLinesFilter(
            bankReconciliation.getIncludeOtherBankStatements(), includeBankStatement);
    Query<BankStatementLine> bankStatementLinesQuery =
        JPA.all(BankStatementLine.class)
            .bind("bankDetails", bankReconciliation.getBankDetails())
            .bind("currency", bankReconciliation.getCurrency())
            .bind("statusImported", BankStatementRepository.STATUS_IMPORTED)
            .bind("bankStatement", bankStatement)
            .bind("bankStatementFileFormat", bankStatement.getBankStatementFileFormat())
            .order("valueDate")
            .order("sequence");
    List<Long> existingBankStatementLineIds = getExistingBankStatementLines(bankReconciliation);
    if (!CollectionUtils.isEmpty(existingBankStatementLineIds)) {
      queryFilter += " AND self.id NOT IN (:existingBankStatementLines)";
      bankStatementLinesQuery.bind("existingBankStatementLines", existingBankStatementLineIds);
    }
    return bankStatementLinesQuery.filter(queryFilter).fetch();
  }

  protected List<Long> getExistingBankStatementLines(BankReconciliation bankReconciliation) {
    List<Long> bankStatementLineIds = Lists.newArrayList();
    List<BankReconciliationLine> bankReconciliationLines =
        bankReconciliation.getBankReconciliationLineList();
    if (CollectionUtils.isEmpty(bankReconciliationLines)) {
      return bankStatementLineIds;
    }
    for (BankReconciliationLine bankReconciliationLine : bankReconciliationLines) {
      if (bankReconciliationLine.getBankStatementLine() != null) {
        bankStatementLineIds.add(bankReconciliationLine.getBankStatementLine().getId());
      }
    }
    return bankStatementLineIds;
  }
}
