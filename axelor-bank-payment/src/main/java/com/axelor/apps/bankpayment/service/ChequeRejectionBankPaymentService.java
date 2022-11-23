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
package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.ChequeRejection;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.ChequeRejectionRepository;
import com.axelor.apps.account.service.ChequeRejectionService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveReverseService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherCancelService;
import com.axelor.apps.bankpayment.db.InterbankCodeLine;
import com.axelor.apps.base.service.administration.SequenceService;
import com.google.inject.Inject;

public class ChequeRejectionBankPaymentService extends ChequeRejectionService {

  @Inject
  public ChequeRejectionBankPaymentService(
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveLineCreateService moveLineCreateService,
      SequenceService sequenceService,
      AccountConfigService accountConfigService,
      ChequeRejectionRepository chequeRejectionRepository,
      MoveReverseService moveReverseService,
      PaymentVoucherCancelService paymentVoucherCancelService) {
    super(
        moveCreateService,
        moveValidateService,
        moveLineCreateService,
        sequenceService,
        accountConfigService,
        chequeRejectionRepository,
        moveReverseService,
        paymentVoucherCancelService);
  }

  @Override
  protected void fillMoveLineFields(ChequeRejection chequeRejection, MoveLine moveLine) {
    super.fillMoveLineFields(chequeRejection, moveLine);

    InterbankCodeLine interbankCodeLine = chequeRejection.getInterbankCodeLine();
    moveLine.setInterbankCodeLine(interbankCodeLine);
  }
}
