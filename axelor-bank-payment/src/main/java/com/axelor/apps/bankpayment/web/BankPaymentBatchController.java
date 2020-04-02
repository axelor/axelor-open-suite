/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.web;

import com.axelor.apps.bankpayment.db.BankPaymentBatch;
import com.axelor.apps.bankpayment.db.repo.BankPaymentBatchRepository;
import com.axelor.apps.bankpayment.service.batch.BatchBankPayment;
import com.axelor.apps.base.db.Batch;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class BankPaymentBatchController {

  @Inject private BankPaymentBatchRepository bankPaymentBatchRepo;

  @Inject private BatchBankPayment batchBankPayment;

  public void launchBankPaymentBatch(ActionRequest request, ActionResponse response)
      throws AxelorException {

    BankPaymentBatch ebicsCertificateBatch = request.getContext().asType(BankPaymentBatch.class);

    Batch batch = null;

    batch =
        batchBankPayment.ebicsCertificate(bankPaymentBatchRepo.find(ebicsCertificateBatch.getId()));

    if (batch != null) response.setFlash(batch.getComments());
    response.setReload(true);
  }
}
