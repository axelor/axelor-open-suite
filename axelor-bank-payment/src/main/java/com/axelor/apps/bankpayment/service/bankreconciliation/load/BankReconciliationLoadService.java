/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationLineService;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class BankReconciliationLoadService {

  protected BankReconciliationLineService bankReconciliationLineService;

  @Inject
  public BankReconciliationLoadService(
      BankReconciliationLineService bankReconciliationLineService) {

    this.bankReconciliationLineService = bankReconciliationLineService;
  }

  @Transactional
  public void loadBankStatement(BankReconciliation bankReconciliation) {

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
            "self.bankDetails = ?1 and self.currency = ?2 and self.amountRemainToReconcile > 0 and self.bankStatement.statusSelect = ?3 "
                + "and ((self.bankStatement = ?4 and ?5 is false) or (self.bankStatement.bankStatementFileFormat = ?6 and ?5 is true))",
            bankReconciliation.getBankDetails(),
            bankReconciliation.getCurrency(),
            BankStatementRepository.STATUS_IMPORTED,
            bankStatement,
            bankReconciliation.getIncludeOtherBankStatements(),
            bankStatement.getBankStatementFileFormat())
        .order("valueDate, sequence")
        .fetch();
  }
}
