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
package com.axelor.apps.bankpayment.service.bankreconciliation;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.db.BankReconciliationLine;
import com.axelor.apps.bankpayment.service.CurrencyScaleServiceBankPayment;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class BankReconciliationSelectedLineComputationServiceImpl
    implements BankReconciliationSelectedLineComputationService {

  protected BankReconciliationQueryService bankReconciliationQueryService;
  protected MoveLineRepository moveLineRepository;
  protected CurrencyScaleServiceBankPayment currencyScaleServiceBankPayment;

  @Inject
  public BankReconciliationSelectedLineComputationServiceImpl(
      BankReconciliationQueryService bankReconciliationQueryService,
      MoveLineRepository moveLineRepository,
      CurrencyScaleServiceBankPayment currencyScaleServiceBankPayment) {
    this.bankReconciliationQueryService = bankReconciliationQueryService;
    this.moveLineRepository = moveLineRepository;
    this.currencyScaleServiceBankPayment = currencyScaleServiceBankPayment;
  }

  @Override
  public BigDecimal computeBankReconciliationLinesSelection(BankReconciliation bankReconciliation) {
    return currencyScaleServiceBankPayment.getScaledValue(
        bankReconciliation,
        bankReconciliation.getBankReconciliationLineList().stream()
            .filter(BankReconciliationLine::getIsSelectedBankReconciliation)
            .map(it -> it.getCredit().subtract(it.getDebit()))
            .reduce(BigDecimal.ZERO, BigDecimal::add));
  }

  @Override
  public BigDecimal computeUnreconciledMoveLinesSelection(BankReconciliation bankReconciliation)
      throws AxelorException {
    String filter = bankReconciliationQueryService.getRequestMoveLines();
    filter = filter.concat(" AND self.isSelectedBankReconciliation = true");
    List<MoveLine> unreconciledMoveLines =
        moveLineRepository
            .all()
            .filter(filter)
            .bind(bankReconciliationQueryService.getBindRequestMoveLine(bankReconciliation))
            .fetch();
    return currencyScaleServiceBankPayment.getScaledValue(
        bankReconciliation,
        unreconciledMoveLines.stream()
            .filter(MoveLine::getIsSelectedBankReconciliation)
            .map(MoveLine::getCurrencyAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
  }

  @Override
  public BigDecimal getSelectedMoveLineTotal(
      BankReconciliation bankReconciliation, List<LinkedHashMap> toReconcileMoveLineSet) {
    BigDecimal selectedMoveLineTotal = BigDecimal.ZERO;
    List<MoveLine> moveLineList = new ArrayList<>();
    toReconcileMoveLineSet.forEach(
        m ->
            moveLineList.add(
                moveLineRepository.find(
                    Long.valueOf((Integer) ((LinkedHashMap<?, ?>) m).get("id")))));
    for (MoveLine moveLine : moveLineList) {
      selectedMoveLineTotal = selectedMoveLineTotal.add(moveLine.getCurrencyAmount().abs());
    }
    return currencyScaleServiceBankPayment.getScaledValue(
        bankReconciliation, selectedMoveLineTotal);
  }
}
