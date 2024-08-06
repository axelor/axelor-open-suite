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
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class MoveLineCheckBankPaymentServiceImpl implements MoveLineCheckBankPaymentService {
  protected MoveLineToolBankPaymentService moveLineToolBankPaymentService;

  @Inject
  public MoveLineCheckBankPaymentServiceImpl(
      MoveLineToolBankPaymentService moveLineToolBankPaymentService) {
    this.moveLineToolBankPaymentService = moveLineToolBankPaymentService;
  }

  @Override
  public void checkBankReconciledAmount(MoveLine moveLine) throws AxelorException {
    if (moveLineToolBankPaymentService.checkBankReconciledAmount(moveLine)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(BankPaymentExceptionMessage.MOVE_LINE_CHECK_BANK_RECONCILED_AMOUNT));
    }
  }
}
