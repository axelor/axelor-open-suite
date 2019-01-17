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
package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;

public class BankReconciliationLineService {

  public BankReconciliationLine createBankReconciliationLine(BankStatementLine bankStatementLine) {

    BankReconciliationLine bankReconciliationLine = new BankReconciliationLine();
    bankReconciliationLine.setEffectDate(bankStatementLine.getValueDate());
    bankReconciliationLine.setDebit(bankStatementLine.getDebit());
    bankReconciliationLine.setCredit(bankStatementLine.getCredit());
    bankReconciliationLine.setName(bankStatementLine.getDescription());
    bankReconciliationLine.setReference(bankStatementLine.getReference());
    bankReconciliationLine.setBankStatementLine(bankStatementLine);

    return bankReconciliationLine;
  }

  public void checkAmount(BankReconciliationLine bankReconciliationLine) throws AxelorException {

    MoveLine moveLine = bankReconciliationLine.getMoveLine();

    BigDecimal bankDebit = bankReconciliationLine.getDebit();
    BigDecimal bankCredit = bankReconciliationLine.getCredit();
    BigDecimal moveLineDebit = moveLine.getDebit();
    BigDecimal moveLineCredit = moveLine.getCredit();

    if (bankDebit.add(bankCredit).compareTo(BigDecimal.ZERO) == 0) {
      throw new AxelorException(
          bankReconciliationLine,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.BANK_STATEMENT_3),
          I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION),
          bankReconciliationLine.getReference() != null
              ? bankReconciliationLine.getReference()
              : "");
    }

    if (!(bankDebit.compareTo(BigDecimal.ZERO) > 0
            && moveLineCredit.compareTo(BigDecimal.ZERO) > 0
            && bankDebit.compareTo(moveLineCredit.subtract(moveLine.getBankReconciledAmount()))
                == 0)
        && !(bankCredit.compareTo(BigDecimal.ZERO) > 0
            && moveLineDebit.compareTo(BigDecimal.ZERO) > 0
            && bankCredit.compareTo(moveLineDebit.subtract(moveLine.getBankReconciledAmount()))
                == 0)) {
      throw new AxelorException(
          bankReconciliationLine,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.BANK_STATEMENT_2),
          I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION),
          bankReconciliationLine.getReference() != null
              ? bankReconciliationLine.getReference()
              : "");
    }
  }
}
