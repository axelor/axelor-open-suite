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
package com.axelor.apps.bankpayment.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveReverseServiceImpl;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.Map;

public class MoveReverseServiceBankPaymentImpl extends MoveReverseServiceImpl {

  @Inject
  public MoveReverseServiceBankPaymentImpl(
      MoveCreateService moveCreateService,
      ReconcileService reconcileService,
      MoveValidateService moveValidateService,
      MoveRepository moveRepository,
      MoveLineCreateService moveLineCreateService) {
    super(
        moveCreateService,
        reconcileService,
        moveValidateService,
        moveRepository,
        moveLineCreateService);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public Move generateReverse(Move move, Map<String, Object> assistantMap) throws AxelorException {
    Move newMove = super.generateReverse(move, assistantMap);

    boolean isHiddenMoveLinesInBankReconciliation =
        (boolean) assistantMap.get("isHiddenMoveLinesInBankReconciliation");
    if (isHiddenMoveLinesInBankReconciliation) {
      move = this.updateBankAmountReconcile(move);
    }
    return newMove;
  }

  @Override
  protected MoveLine generateReverseMoveLine(
      Move reverseMove, MoveLine orgineMoveLine, LocalDate dateOfReversion, boolean isDebit)
      throws AxelorException {
    MoveLine reverseMoveLine =
        super.generateReverseMoveLine(reverseMove, orgineMoveLine, dateOfReversion, isDebit);
    reverseMoveLine.setBankReconciledAmount(
        reverseMoveLine
            .getDebit()
            .add(reverseMoveLine.getCredit().subtract(orgineMoveLine.getBankReconciledAmount())));
    return reverseMoveLine;
  }

  protected Move updateBankAmountReconcile(Move move) {
    for (MoveLine moveLine : move.getMoveLineList()) {
      moveLine.setBankReconciledAmount(moveLine.getDebit().add(moveLine.getCredit()));
    }
    return move;
  }
}
