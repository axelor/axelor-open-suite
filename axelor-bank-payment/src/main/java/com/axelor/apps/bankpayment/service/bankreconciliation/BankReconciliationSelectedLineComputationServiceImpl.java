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
import com.axelor.apps.bankpayment.service.BankReconciliationToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;

public class BankReconciliationSelectedLineComputationServiceImpl
    implements BankReconciliationSelectedLineComputationService {

  protected BankReconciliationQueryService bankReconciliationQueryService;
  protected MoveLineRepository moveLineRepository;
  protected CurrencyScaleService currencyScaleService;

  @Inject
  public BankReconciliationSelectedLineComputationServiceImpl(
      BankReconciliationQueryService bankReconciliationQueryService,
      MoveLineRepository moveLineRepository,
      CurrencyScaleService currencyScaleService) {
    this.bankReconciliationQueryService = bankReconciliationQueryService;
    this.moveLineRepository = moveLineRepository;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public BigDecimal computeBankReconciliationLinesSelection(BankReconciliation bankReconciliation) {
    return currencyScaleService.getScaledValue(
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
    return currencyScaleService.getScaledValue(
        bankReconciliation,
        unreconciledMoveLines.stream()
            .filter(MoveLine::getIsSelectedBankReconciliation)
            .map(MoveLine::getCurrencyAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
  }

  @Override
  @SuppressWarnings("rawtypes")
  public BigDecimal getSelectedMoveLineTotal(
      BankReconciliation bankReconciliation, List<LinkedHashMap> toReconcileMoveLineSet) {
    BigDecimal selectedMoveLineTotal =
        toReconcileMoveLineSet.stream()
            .map(map -> map.get("id"))
            .map(Object::toString)
            .map(Long::valueOf)
            .map(moveLineRepository::find)
            .map(l -> getMoveLineAmount(l, bankReconciliation))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    return currencyScaleService.getScaledValue(bankReconciliation, selectedMoveLineTotal);
  }

  protected BigDecimal getMoveLineAmount(MoveLine moveLine, BankReconciliation bankReconciliation) {
    return BankReconciliationToolService.isForeignCurrency(bankReconciliation)
        ? moveLine.getCurrencyAmount().abs()
        : moveLine.getDebit().add(moveLine.getCredit());
  }
}
