/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationRepository;
import com.axelor.apps.bankpayment.service.moveline.MoveLinePostedNbrService;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.common.StringUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public class BankReconciliationLineUnreconciliationServiceImpl
    implements BankReconciliationLineUnreconciliationService {

  protected MoveLineRepository moveLineRepository;
  protected CurrencyScaleService currencyScaleService;
  protected MoveLinePostedNbrService moveLinePostedNbrService;

  @Inject
  public BankReconciliationLineUnreconciliationServiceImpl(
      MoveLineRepository moveLineRepository,
      CurrencyScaleService currencyScaleService,
      MoveLinePostedNbrService moveLinePostedNbrService) {
    this.moveLineRepository = moveLineRepository;
    this.currencyScaleService = currencyScaleService;
    this.moveLinePostedNbrService = moveLinePostedNbrService;
  }

  @Override
  public void unreconcileLines(List<BankReconciliationLine> bankReconciliationLines) {
    for (BankReconciliationLine bankReconciliationLine : bankReconciliationLines) {
      if (StringUtils.notEmpty((bankReconciliationLine.getPostedNbr()))) {
        unreconcileLine(bankReconciliationLine);
      }
    }
  }

  @Override
  @Transactional
  public void unreconcileLine(BankReconciliationLine bankReconciliationLine) {
    bankReconciliationLine.setBankStatementQuery(null);
    bankReconciliationLine.setIsSelectedBankReconciliation(false);

    String query = "self.postedNbr LIKE '%%s%'";
    query = query.replace("%s", bankReconciliationLine.getPostedNbr());
    List<MoveLine> moveLines = moveLineRepository.all().filter(query).fetch();
    for (MoveLine moveLine : moveLines) {
      moveLinePostedNbrService.removePostedNbr(moveLine, bankReconciliationLine.getPostedNbr());
      moveLine.setIsSelectedBankReconciliation(false);
    }
    boolean isUnderCorrection =
        bankReconciliationLine.getBankReconciliation().getStatusSelect()
            == BankReconciliationRepository.STATUS_UNDER_CORRECTION;
    if (isUnderCorrection) {
      MoveLine moveLine = bankReconciliationLine.getMoveLine();
      BankStatementLine bankStatementLine = bankReconciliationLine.getBankStatementLine();
      if (bankStatementLine != null) {
        bankStatementLine.setAmountRemainToReconcile(
            currencyScaleService.getScaledValue(
                bankStatementLine,
                bankStatementLine
                    .getAmountRemainToReconcile()
                    .add(moveLine.getBankReconciledAmount())));
      }
      moveLine.setBankReconciledAmount(BigDecimal.ZERO);
      moveLineRepository.save(moveLine);
      bankReconciliationLine.setIsPosted(false);
    }
    bankReconciliationLine.setMoveLine(null);
    bankReconciliationLine.setConfidenceIndex(0);
    bankReconciliationLine.setPostedNbr(null);
  }
}
