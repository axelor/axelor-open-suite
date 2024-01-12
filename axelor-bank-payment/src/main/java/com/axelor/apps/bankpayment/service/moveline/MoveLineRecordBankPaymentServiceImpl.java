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
package com.axelor.apps.bankpayment.service.moveline;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class MoveLineRecordBankPaymentServiceImpl implements MoveLineRecordBankPaymentService {
  MoveLineToolBankPaymentService moveLineToolBankPaymentService;
  MoveLineRepository moveLineRepo;

  @Inject
  public MoveLineRecordBankPaymentServiceImpl(
      MoveLineToolBankPaymentService moveLineToolBankPaymentService,
      MoveLineRepository moveLineRepo) {
    this.moveLineToolBankPaymentService = moveLineToolBankPaymentService;
    this.moveLineRepo = moveLineRepo;
  }

  @Override
  public void revertDebitCreditAmountChange(MoveLine moveLine) {
    if (!moveLineToolBankPaymentService.checkBankReconciledAmount(moveLine)) {
      return;
    }

    if (moveLine.getId() == null) {
      MoveLine savedMoveLine = moveLineRepo.find(moveLine.getId());
      moveLine.setDebit(savedMoveLine.getDebit());
      moveLine.setCredit(savedMoveLine.getCredit());
    } else {
      moveLine.setDebit(BigDecimal.ZERO);
      moveLine.setCredit(BigDecimal.ZERO);
    }
  }

  @Override
  public void revertBankReconciledAmountChange(MoveLine moveLine) {
    if (!moveLineToolBankPaymentService.checkBankReconciledAmount(moveLine)) {
      return;
    }

    if (moveLine.getId() == null) {
      MoveLine savedMoveLine = moveLineRepo.find(moveLine.getId());
      moveLine.setBankReconciledAmount(savedMoveLine.getBankReconciledAmount());
    } else {
      moveLine.setBankReconciledAmount(BigDecimal.ZERO);
    }
  }
}
