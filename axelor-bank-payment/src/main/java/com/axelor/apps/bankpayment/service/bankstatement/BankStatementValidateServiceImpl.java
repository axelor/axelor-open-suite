/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BankStatementValidateServiceImpl implements BankStatementValidateService {
  BankStatementRepository bankStatementRepository;
  protected BankStatementLineRepository bankStatementLineRepository;

  @Inject
  public BankStatementValidateServiceImpl(
      BankStatementRepository bankStatementRepository,
      BankStatementLineRepository bankStatementLineRepository) {
    this.bankStatementRepository = bankStatementRepository;
    this.bankStatementLineRepository = bankStatementLineRepository;
  }

  @Transactional
  public BankStatement setIsFullyReconciled(BankStatement bankStatement) {
    List<BankStatementLine> bankStatementLines =
        bankStatementLineRepository.findByBankStatement(bankStatement).fetch();
    BigDecimal amountToReconcile = BigDecimal.ZERO;
    for (BankStatementLine bankStatementLine : bankStatementLines) {
      amountToReconcile = amountToReconcile.add(bankStatementLine.getAmountRemainToReconcile());
    }
    bankStatement.setIsFullyReconciled(amountToReconcile.compareTo(BigDecimal.ZERO) == 0);

    return bankStatementRepository.save(bankStatement);
  }

  @Override
  public void setIsFullyReconciled(BankReconciliation bankReconciliation) {
    Set<BankStatement> bankStatements = new LinkedHashSet<>();
    if (bankReconciliation.getBankStatement() != null) {
      bankStatements.add(bankReconciliation.getBankStatement());
    }
    if (bankReconciliation.getBankReconciliationLineList() != null) {
      for (BankReconciliationLine bankReconciliationLine :
          bankReconciliation.getBankReconciliationLineList()) {
        BankStatementLine bankStatementLine = bankReconciliationLine.getBankStatementLine();
        if (bankStatementLine != null && bankStatementLine.getBankStatement() != null) {
          bankStatements.add(bankStatementLine.getBankStatement());
        }
      }
    }
    for (BankStatement bankStatement : bankStatements) {
      setIsFullyReconciled(bankStatement);
    }
  }
}
