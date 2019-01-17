/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankreconciliation.load.afb120;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationLineService;
import com.axelor.apps.bankpayment.service.bankreconciliation.load.BankReconciliationLoadService;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

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
  public void loadBankStatement(BankReconciliation bankReconciliation) {

    BankStatementLine initialBalanceBankStatementLine =
        getInitialBalanceBankStatementLine(bankReconciliation);
    BankStatementLine finalBalanceBankStatementLine =
        getFinalBalanceBankStatementLine(bankReconciliation);

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
    this.loadBankStatementLines(bankReconciliation);
  }

  @Transactional
  public void loadBankStatementLines(BankReconciliation bankReconciliation) {

    List<BankStatementLine> bankStatementLineList = getBankStatementLines(bankReconciliation);

    if (bankStatementLineList != null) {
      for (BankStatementLine bankStatementLine : bankStatementLineList) {

        bankReconciliation.addBankReconciliationLineListItem(
            bankReconciliationLineService.createBankReconciliationLine(bankStatementLine));
      }
    }
  }

  protected List<BankStatementLine> getBankStatementLines(BankReconciliation bankReconciliation) {

    BankStatement bankStatement = bankReconciliation.getBankStatement();

    return JPA.all(BankStatementLine.class)
        .filter(
            "self.bankDetails = ?1 and self.currency = ?2 and self.amountRemainToReconcile > 0 and self.bankStatement.statusSelect = ?3"
                + " and ((self.bankStatement = ?4 and ?5 is false) or (self.bankStatement.bankStatementFileFormat = ?6 and ?5 is true))"
                + " and self.lineTypeSelect = ?7 ",
            bankReconciliation.getBankDetails(),
            bankReconciliation.getCurrency(),
            BankStatementRepository.STATUS_IMPORTED,
            bankStatement,
            bankReconciliation.getIncludeOtherBankStatements(),
            bankStatement.getBankStatementFileFormat(),
            BankStatementLineAFB120Repository.LINE_TYPE_MOVEMENT)
        .order("valueDate, sequence")
        .fetch();
  }

  protected BankStatementLine getInitialBalanceBankStatementLine(
      BankReconciliation bankReconciliation) {

    BankStatement bankStatement = bankReconciliation.getBankStatement();

    return JPA.all(BankStatementLine.class)
        .filter(
            "self.bankDetails = ?1 and self.currency = ?2 and self.amountRemainToReconcile > 0 and self.bankStatement.statusSelect = ?3"
                + " and ((self.bankStatement = ?4 and ?5 is false) or (self.bankStatement.bankStatementFileFormat = ?6 and ?5 is true))"
                + " and self.lineTypeSelect = ?7 ",
            bankReconciliation.getBankDetails(),
            bankReconciliation.getCurrency(),
            BankStatementRepository.STATUS_IMPORTED,
            bankStatement,
            bankReconciliation.getIncludeOtherBankStatements(),
            bankStatement.getBankStatementFileFormat(),
            BankStatementLineAFB120Repository.LINE_TYPE_INITIAL_BALANCE)
        .order("operationDate, sequence")
        .fetchOne();
  }

  protected BankStatementLine getFinalBalanceBankStatementLine(
      BankReconciliation bankReconciliation) {

    BankStatement bankStatement = bankReconciliation.getBankStatement();

    return JPA.all(BankStatementLine.class)
        .filter(
            "self.bankDetails = ?1 and self.currency = ?2 and self.amountRemainToReconcile > 0 and self.bankStatement.statusSelect = ?3"
                + " and ((self.bankStatement = ?4 and ?5 is false) or (self.bankStatement.bankStatementFileFormat = ?6 and ?5 is true))"
                + " and self.lineTypeSelect = ?7 ",
            bankReconciliation.getBankDetails(),
            bankReconciliation.getCurrency(),
            BankStatementRepository.STATUS_IMPORTED,
            bankStatement,
            bankReconciliation.getIncludeOtherBankStatements(),
            bankStatement.getBankStatementFileFormat(),
            BankStatementLineAFB120Repository.LINE_TYPE_FINAL_BALANCE)
        .order("-operationDate, -sequence")
        .fetchOne();
  }
}
