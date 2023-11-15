/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationLineService;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationService;
import com.axelor.apps.bankpayment.service.bankreconciliation.load.BankReconciliationLoadBankStatementAbstractService;
import com.axelor.apps.bankpayment.service.bankstatementline.BankStatementLineFilterService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class BankReconciliationLoadBankStatementAFB120Service
    extends BankReconciliationLoadBankStatementAbstractService {

  protected BankStatementLineAFB120Repository bankStatementLineAFB120Repository;
  protected BankStatementLineFilterService bankStatementLineFilterService;
  protected BankReconciliationLineService bankReconciliationLineService;

  @Inject
  public BankReconciliationLoadBankStatementAFB120Service(
      BankReconciliationRepository bankReconciliationRepository,
      BankReconciliationService bankReconciliationService,
      BankStatementLineAFB120Repository bankStatementLineAFB120Repository,
      BankStatementLineFilterService bankStatementLineFilterService,
      BankReconciliationLineService bankReconciliationLineService) {
    super(bankReconciliationRepository, bankReconciliationService);
    this.bankStatementLineAFB120Repository = bankStatementLineAFB120Repository;
    this.bankStatementLineFilterService = bankStatementLineFilterService;
    this.bankReconciliationLineService = bankReconciliationLineService;
  }

  @Transactional
  public void loadBankStatement(
      BankReconciliation bankReconciliation, boolean includeBankStatement) {

    setBalance(bankReconciliation, includeBankStatement);

    this.loadBankStatementLines(bankReconciliation, includeBankStatement);
  }

  protected void setBalance(BankReconciliation bankReconciliation, boolean includeBankStatement) {
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
  }

  @Transactional
  protected void loadBankStatementLines(
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

  protected List<BankStatementLineAFB120> getBankStatementLinesAFB120(
      BankReconciliation bankReconciliation, boolean includeBankStatement) {

    BankStatement bankStatement = bankReconciliation.getBankStatement();
    String queryFilter =
        bankStatementLineFilterService.getBankStatementLinesAFB120FilterWithAmountToReconcile(
            bankReconciliation.getIncludeOtherBankStatements(), includeBankStatement);
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
            bankStatementLineFilterService.getBankStatementLinesAFB120Filter(
                bankReconciliation.getIncludeOtherBankStatements(), includeBankStatement))
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
            bankStatementLineFilterService.getBankStatementLinesAFB120Filter(
                bankReconciliation.getIncludeOtherBankStatements(), includeBankStatement))
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
